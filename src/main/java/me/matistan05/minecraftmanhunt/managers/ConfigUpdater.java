package me.matistan05.minecraftmanhunt.managers;

import dev.dejvokep.boostedyaml.YamlDocument;
import me.matistan05.minecraftmanhunt.Main;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;

public final class ConfigUpdater {
    private static final String CONFIG_FILE_NAME = "config.yml";
    private static final String BACKUP_SUFFIX = ".bak";

    private final Main main;

    public ConfigUpdater(Main main) {
        this.main = main;
    }

    public void updateOnStartup() {
        Path configPath = configFilePath();
        Path backupPath = backupFilePath();
        try {
            Files.createDirectories(main.getDataFolder().toPath());
            if (Files.exists(configPath)) {
                createBackup(configPath, backupPath);
            }

            try (InputStream defaults = main.getResource(CONFIG_FILE_NAME)) {
                if (defaults == null) {
                    main.getLogger().severe("Не удалось найти встроенный config.yml в jar.");
                    return;
                }

                YamlDocument document = YamlDocument.create(configPath.toFile(), defaults);
                migrateLegacyStructure(document);
                document.update();
                document.save();
            }
        } catch (Exception exception) {
            main.getLogger().severe("Не удалось обновить config.yml через BoostedYAML: "
                    + exception.getMessage());
            main.getLogger().log(Level.SEVERE, "Stacktrace:", exception);
            restoreBackupIfPresent(configPath, backupPath);
        } finally {
            main.reloadConfig();
        }
    }

    public boolean setValue(String route, Object value) {
        Path configPath = configFilePath();
        Path backupPath = backupFilePath();
        try {
            Files.createDirectories(main.getDataFolder().toPath());
            if (Files.exists(configPath)) {
                createBackup(configPath, backupPath);
            }

            try (InputStream defaults = main.getResource(CONFIG_FILE_NAME)) {
                if (defaults == null) {
                    main.getLogger().severe("Не удалось найти встроенный config.yml в jar.");
                    return false;
                }

                YamlDocument document = YamlDocument.create(configPath.toFile(), defaults);
                migrateLegacyStructure(document);
                document.update();
                document.set(route, value);
                document.save();
            }

            main.reloadConfig();
            return true;
        } catch (Exception exception) {
            main.getLogger().severe("Не удалось сохранить config.yml через BoostedYAML: "
                    + exception.getMessage());
            main.getLogger().log(Level.SEVERE, "Stacktrace:", exception);
            restoreBackupIfPresent(configPath, backupPath);
            main.reloadConfig();
            return false;
        }
    }

    private void createBackup(Path configPath, Path backupPath) throws IOException {
        Files.copy(configPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
    }

    private Path configFilePath() {
        return main.getDataFolder().toPath().resolve(CONFIG_FILE_NAME);
    }

    private Path backupFilePath() {
        return configFilePath().resolveSibling(CONFIG_FILE_NAME + BACKUP_SUFFIX);
    }

    private void migrateLegacyStructure(YamlDocument document) {
        migrateValue(document, "timeSetDayOnStart", "gameplay.day-on-start");
        migrateValue(document, "weatherClearOnStart", "gameplay.weather-clear-on-start");
        migrateValue(document, "headStartDuration", "gameplay.head-start-seconds");
        migrateValue(document, "speedrunnersLives", "gameplay.speedrunners-lives");
        migrateValue(document, "spectatorAfterDeath", "gameplay.spectator-after-death");
        migrateValue(document, "teleport", "gameplay.teleport-on-start");
        migrateValue(document, "friendlyFire", "gameplay.friendly-fire");
        migrateValue(document, "clearInventories", "gameplay.clear-inventories");
        migrateValue(document, "takeAwayOps", "gameplay.take-away-ops");
        migrateValue(document, "enablePauses", "gameplay.pause-enabled");
        migrateValue(document, "fastplavka", "gameplay.fast-furnace-speed");
        migrateValue(document, "usePermissions", "permissions.enabled");
        migrateValue(document, "compassMenu", "compass.menu-enabled");
        migrateValue(document, "trackNearestMode", "compass.track-nearest-mode");
        migrateValue(document, "trackPortals", "compass.track-portals");
        migrateValue(document, "useBossBarRadar", "compass.bossbar-radar");
        migrateValue(document, "matchWorlds.enabled", "match-worlds.enabled");
        migrateValue(document, "matchWorlds.autoGenerate.worldPrefix", "match-worlds.auto-generate.world-prefix");
        migrateValue(document, "matchWorlds.autoGenerate.keepLatestWorlds", "match-worlds.auto-generate.keep-latest-worlds");
        migrateValue(document, "matchWorlds.autoGenerate.maxAttempts", "match-worlds.auto-generate.max-attempts");
        migrateValue(document, "matchWorlds.autoGenerate.minScoreToAccept", "match-worlds.auto-generate.min-score-to-accept");
        migrateValue(document, "matchWorlds.autoGenerate.acceptBestCandidateIfThresholdMissed",
                "match-worlds.auto-generate.accept-best-candidate-if-threshold-missed");
        migrateValue(document, "matchWorlds.autoGenerate.fixedSeeds", "match-worlds.auto-generate.fixed-seeds");
        migrateValue(document, "matchWorlds.autoGenerate.structureSearchRadiusChunks",
                "match-worlds.auto-generate.structure-search-radius-chunks");
        migrateValue(document, "matchWorlds.autoGenerate.rareStructureSearchRadiusChunks",
                "match-worlds.auto-generate.rare-structure-search-radius-chunks");
        migrateValue(document, "matchWorlds.autoGenerate.startDistanceFromAnchor",
                "match-worlds.auto-generate.start-distance-from-anchor");
        migrateValue(document, "matchWorlds.autoGenerate.preferVillageWithSmithBuilding",
                "match-worlds.auto-generate.village.prefer-smith-building");
        migrateValue(document, "matchWorlds.autoGenerate.requireVillageWithSmithBuilding",
                "match-worlds.auto-generate.village.require-smith-building");
        migrateValue(document, "matchWorlds.autoGenerate.villageSmithSearchRadiusBlocks",
                "match-worlds.auto-generate.village.smith-search-radius-blocks");
        migrateValue(document, "matchWorlds.autoGenerate.anchorWeights.village",
                "match-worlds.auto-generate.anchor-weights.village");
        migrateValue(document, "matchWorlds.autoGenerate.anchorWeights.ruinedPortal",
                "match-worlds.auto-generate.anchor-weights.ruined-portal");
        migrateValue(document, "matchWorlds.autoGenerate.anchorWeights.shipwreck",
                "match-worlds.auto-generate.anchor-weights.shipwreck");
        migrateValue(document, "matchWorlds.autoGenerate.anchorWeights.pillagerOutpost",
                "match-worlds.auto-generate.anchor-weights.pillager-outpost");
        migrateValue(document, "matchWorlds.autoGenerate.anchorWeights.woodlandMansion",
                "match-worlds.auto-generate.anchor-weights.woodland-mansion");
        migrateValue(document, "casual", "casual.enabled");
        migrateValue(document, "warpShadowsCooldown", "casual.warp-shadows.cooldown-seconds");
        migrateValue(document, "warpShadowsMaxDistance", "casual.warp-shadows.max-distance");
        migrateValue(document, "warpShadowsBufferZone", "casual.warp-shadows.buffer-zone");
        migrateValue(document, "update.enabled", "updates.enabled");
        migrateValue(document, "update.checkOnStartup", "updates.check-on-startup");
        migrateValue(document, "update.owner", "updates.github.owner");
        migrateValue(document, "update.repository", "updates.github.repository");
        migrateValue(document, "update.allowPrerelease", "updates.github.allow-prerelease");
        migrateValue(document, "update.assetName", "updates.github.asset-name");
    }

    private void migrateValue(YamlDocument document, String sourcePath, String targetPath) {
        if (!document.contains(sourcePath)) {
            return;
        }

        if ("casual".equals(sourcePath) && document.isSection(sourcePath)) {
            return;
        }

        Object value = document.get(sourcePath);
        boolean conflictsWithTarget = targetPath.startsWith(sourcePath + ".");

        if (conflictsWithTarget) {
            document.remove(sourcePath);
            if (!document.contains(targetPath)) {
                document.set(targetPath, value);
            }
            return;
        }

        if (!document.contains(targetPath)) {
            document.set(targetPath, value);
        }
        document.remove(sourcePath);
    }

    private void restoreBackupIfPresent(Path configPath, Path backupPath) {
        if (!Files.exists(backupPath)) {
            return;
        }

        try {
            Files.copy(backupPath, configPath, StandardCopyOption.REPLACE_EXISTING);
            main.getLogger().warning("config.yml был восстановлен из backup после неудачного обновления.");
        } catch (IOException restoreException) {
            main.getLogger().log(Level.SEVERE, "Не удалось восстановить config.yml из backup.", restoreException);
        }
    }
}
