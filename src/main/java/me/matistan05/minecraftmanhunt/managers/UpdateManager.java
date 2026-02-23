package me.matistan05.minecraftmanhunt.managers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.matistan05.minecraftmanhunt.Main;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class UpdateManager {
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final String USER_AGENT = "H-Manhunt-Updater";

    private final Main main;
    private final HttpClient httpClient;

    public UpdateManager(Main main) {
        this.main = main;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public void checkOnStartup() {
        if (!isEnabled()) {
            main.getLogger().info("[Updater] Проверка обновлений отключена в config.yml");
            return;
        }
        if (!main.getConfig().getBoolean("update.checkOnStartup", true)) {
            main.getLogger().info("[Updater] Автопроверка при запуске отключена в config.yml");
            return;
        }
        checkAsync(null, false, true);
    }

    public void checkByCommand(CommandSender sender) {
        if (!isEnabled()) {
            sendMessage(sender, "<red>Проверка обновлений отключена в config.yml (update.enabled: false).");
            return;
        }
        checkAsync(sender, false, false);
    }

    public void downloadByCommand(CommandSender sender) {
        if (!isEnabled()) {
            sendMessage(sender, "<red>Проверка обновлений отключена в config.yml (update.enabled: false).");
            return;
        }
        checkAsync(sender, true, false);
    }

    private void checkAsync(CommandSender sender, boolean downloadIfAvailable, boolean startupMode) {
        if (!startupMode) {
            sendMessage(sender, "<gray>Проверяю обновления на GitHub...</gray>");
        } else {
            main.getLogger().info("[Updater] Проверяю обновления на GitHub...");
        }

        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            try {
                ReleaseInfo release = fetchLatestRelease();
                if (release == null) {
                    announceNoRelease(sender, startupMode);
                    return;
                }

                String currentVersion = normalizeVersion(main.getPluginMeta().getVersion());
                String latestVersion = normalizeVersion(release.tagName());

                if (compareVersions(latestVersion, currentVersion) <= 0) {
                    announceNoUpdates(sender, startupMode, currentVersion);
                    return;
                }

                announceUpdateAvailable(sender, startupMode, currentVersion, latestVersion, release.assetName());
                if (!downloadIfAvailable) {
                    return;
                }

                Path downloadedPath = downloadToUpdateFolder(release);
                announceDownloaded(sender, latestVersion, downloadedPath);
            } catch (Exception e) {
                String message = "[Updater] Ошибка при проверке обновлений: " + e.getMessage();
                main.getLogger().warning(message);
                if (!startupMode) {
                    sendMessage(sender, "<red>Ошибка при проверке/скачивании обновления: <white>" + e.getMessage());
                }
            }
        });
    }

    private ReleaseInfo fetchLatestRelease() throws IOException, InterruptedException {
        String owner = main.getConfig().getString("update.owner", "hetasshi");
        String repository = main.getConfig().getString("update.repository", "H-Manhunt");
        boolean allowPrerelease = main.getConfig().getBoolean("update.allowPrerelease", false);

        if (owner == null || owner.isBlank() || repository == null || repository.isBlank()) {
            throw new IllegalStateException("Пустые update.owner/update.repository в config.yml");
        }

        if (!allowPrerelease) {
            String endpoint = "https://api.github.com/repos/" + urlEncode(owner) + "/" + urlEncode(repository)
                    + "/releases/latest";
            JsonObject release = readJson(endpoint).getAsJsonObject();
            return parseRelease(release);
        }

        String endpoint = "https://api.github.com/repos/" + urlEncode(owner) + "/" + urlEncode(repository)
                + "/releases?per_page=15";
        JsonArray releases = readJson(endpoint).getAsJsonArray();
        for (JsonElement element : releases) {
            JsonObject release = element.getAsJsonObject();
            if (release.get("draft") != null && release.get("draft").getAsBoolean()) {
                continue;
            }
            ReleaseInfo info = parseRelease(release);
            if (info != null) {
                return info;
            }
        }
        return null;
    }

    private JsonElement readJson(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .header("User-Agent", USER_AGENT)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        int code = response.statusCode();
        if (code < 200 || code >= 300) {
            throw new IOException("GitHub API вернул HTTP " + code);
        }

        return JsonParser.parseString(response.body());
    }

    private ReleaseInfo parseRelease(JsonObject release) {
        if (release == null) {
            return null;
        }
        JsonElement tagElement = release.get("tag_name");
        JsonArray assets = release.getAsJsonArray("assets");
        if (tagElement == null || tagElement.isJsonNull() || assets == null) {
            return null;
        }

        String preferredAssetName = main.getConfig().getString("update.assetName", "");
        if (preferredAssetName == null) {
            preferredAssetName = "";
        }
        preferredAssetName = preferredAssetName.trim();
        String selectedAssetName = null;
        String selectedDownloadUrl = null;

        for (JsonElement assetElement : assets) {
            JsonObject asset = assetElement.getAsJsonObject();
            JsonElement nameElement = asset.get("name");
            JsonElement urlElement = asset.get("browser_download_url");
            if (nameElement == null || urlElement == null
                    || nameElement.isJsonNull() || urlElement.isJsonNull()) {
                continue;
            }

            String assetName = nameElement.getAsString();
            String downloadUrl = urlElement.getAsString();
            if (assetName.isBlank() || downloadUrl.isBlank()) {
                continue;
            }

            if (!preferredAssetName.isBlank()) {
                if (assetName.equalsIgnoreCase(preferredAssetName)) {
                    selectedAssetName = assetName;
                    selectedDownloadUrl = downloadUrl;
                    break;
                }
                continue;
            }

            if (assetName.toLowerCase().endsWith(".jar")) {
                selectedAssetName = assetName;
                selectedDownloadUrl = downloadUrl;
                break;
            }
        }

        if (selectedAssetName == null || selectedDownloadUrl == null) {
            return null;
        }

        return new ReleaseInfo(tagElement.getAsString(), selectedAssetName, selectedDownloadUrl);
    }

    private Path downloadToUpdateFolder(ReleaseInfo release) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(release.downloadUrl()))
                .timeout(Duration.ofSeconds(45))
                .header("Accept", "application/octet-stream")
                .header("User-Agent", USER_AGENT)
                .GET()
                .build();

        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Не удалось скачать файл обновления (HTTP " + response.statusCode() + ")");
        }
        if (response.body() == null || response.body().length == 0) {
            throw new IOException("Скачан пустой файл обновления");
        }

        Path updateDir = main.getServer().getUpdateFolderFile().toPath();
        Files.createDirectories(updateDir);

        String targetFileName = resolveTargetFileName(release.assetName());
        Path tempFile = updateDir.resolve(targetFileName + ".download");
        Path targetFile = updateDir.resolve(targetFileName);

        Files.write(tempFile, response.body());
        Files.move(tempFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
        return targetFile;
    }

    private String resolveTargetFileName(String downloadedAssetName) {
        String fallbackName = downloadedAssetName;
        if (fallbackName == null || fallbackName.isBlank()) {
            fallbackName = "H-Manhunt.jar";
        }

        try {
            URI location = main.getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
            Path loadedPluginPath = Path.of(location).getFileName();
            if (loadedPluginPath == null) {
                return fallbackName;
            }
            String loadedName = loadedPluginPath.toString();
            if (loadedName.toLowerCase().endsWith(".jar")) {
                return loadedName;
            }
        } catch (Exception ignored) {
        }
        return fallbackName;
    }

    private void announceNoRelease(CommandSender sender, boolean startupMode) {
        String message = "<gray>[Updater] Не удалось найти подходящий релиз с .jar в GitHub Releases.</gray>";
        if (startupMode) {
            main.getLogger().info("[Updater] Не удалось найти подходящий релиз с .jar в GitHub Releases.");
            return;
        }
        sendMessage(sender, message);
    }

    private void announceNoUpdates(CommandSender sender, boolean startupMode, String currentVersion) {
        String message = "<gray>[Updater] Обновлений нет. Текущая версия: <white>" + currentVersion + "</white>.</gray>";
        if (startupMode) {
            main.getLogger().info("[Updater] Обновлений нет. Текущая версия: " + currentVersion + ".");
            return;
        }
        sendMessage(sender, message);
    }

    private void announceUpdateAvailable(CommandSender sender, boolean startupMode, String currentVersion,
                                         String latestVersion, String assetName) {
        String text = "[Updater] Доступно обновление: " + currentVersion + " -> " + latestVersion
                + " (" + assetName + ")";
        if (startupMode) {
            main.getLogger().info(text);
            return;
        }
        sendMessage(sender, "<green>[Updater] Доступно обновление: <white>" + currentVersion + " <gray>-></gray> "
                + latestVersion + "</white> <gray>(" + assetName + ")</gray>");
    }

    private void announceDownloaded(CommandSender sender, String latestVersion, Path downloadedPath) {
        sendMessage(sender, "<green>[Updater] Обновление " + latestVersion + " скачано в: <white>"
                + downloadedPath + "</white>");
        sendMessage(sender, "<gray>[Updater] Перезапустите сервер, чтобы обновление применилось.</gray>");
    }

    private void sendMessage(CommandSender sender, String message) {
        if (sender == null) {
            return;
        }
        Runnable task = () -> sender.sendMessage(MM.deserialize(message));
        if (Bukkit.isPrimaryThread()) {
            task.run();
            return;
        }
        Bukkit.getScheduler().runTask(main, task);
    }

    private boolean isEnabled() {
        return main.getConfig().getBoolean("update.enabled", true);
    }

    private String normalizeVersion(String rawVersion) {
        if (rawVersion == null) {
            return "";
        }
        String normalized = rawVersion.trim();
        if (normalized.startsWith("v") || normalized.startsWith("V")) {
            normalized = normalized.substring(1);
        }
        int plusIndex = normalized.indexOf('+');
        if (plusIndex > -1) {
            normalized = normalized.substring(0, plusIndex);
        }
        return normalized;
    }

    private int compareVersions(String first, String second) {
        List<String> firstParts = splitVersion(first);
        List<String> secondParts = splitVersion(second);
        int max = Math.max(firstParts.size(), secondParts.size());
        for (int i = 0; i < max; i++) {
            String left = i < firstParts.size() ? firstParts.get(i) : "0";
            String right = i < secondParts.size() ? secondParts.get(i) : "0";
            int cmp = comparePart(left, right);
            if (cmp != 0) {
                return cmp;
            }
        }
        return 0;
    }

    private List<String> splitVersion(String version) {
        if (version == null || version.isBlank()) {
            return List.of("0");
        }
        String[] raw = version.split("[.-]");
        List<String> parts = new ArrayList<>();
        for (String part : raw) {
            if (!part.isBlank()) {
                parts.add(part);
            }
        }
        if (parts.isEmpty()) {
            return List.of("0");
        }
        return parts;
    }

    private int comparePart(String left, String right) {
        boolean leftNumeric = left.chars().allMatch(Character::isDigit);
        boolean rightNumeric = right.chars().allMatch(Character::isDigit);
        if (leftNumeric && rightNumeric) {
            return new BigInteger(left).compareTo(new BigInteger(right));
        }
        if (leftNumeric) {
            return 1;
        }
        if (rightNumeric) {
            return -1;
        }
        return left.compareToIgnoreCase(right);
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private record ReleaseInfo(String tagName, String assetName, String downloadUrl) {
    }
}
