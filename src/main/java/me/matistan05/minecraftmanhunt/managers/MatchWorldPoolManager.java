package me.matistan05.minecraftmanhunt.managers;

import me.matistan05.minecraftmanhunt.Main;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

public class MatchWorldPoolManager {
    private static final String POOL_FILE_NAME = "match-world-pool.yml";
    private static final String POOL_ENABLED = "match-worlds.pool.enabled";
    private static final String PREPARE_ON_STARTUP = "match-worlds.pool.prepare-on-startup-if-empty";
    private static final String PREPARE_STARTUP_DELAY_SECONDS = "match-worlds.pool.prepare-startup-delay-seconds";
    private static final String BATCH_SIZE = "match-worlds.pool.batch-size";
    private static final String DELAY_BETWEEN_WORLDS_SECONDS = "match-worlds.pool.delay-between-worlds-seconds";
    private static final String GENERATE_ON_START_IF_EMPTY = "match-worlds.pool.generate-on-start-if-empty";
    private static final String DELETE_USED_WORLDS = "match-worlds.pool.delete-used-worlds";
    private static final String WORLD_PREFIX = "match-worlds.pool.world-prefix";
    private static final String UNLOAD_READY_WORLDS = "match-worlds.pool.unload-ready-worlds";
    private static final String FIXED_SEEDS = "match-worlds.auto-generate.fixed-seeds";

    private final Main main;
    private final MatchWorldLifecycleManager lifecycleManager;
    private final MatchWorldEvaluator evaluator;
    private final Map<String, MatchWorldPoolEntry> entries = new LinkedHashMap<>();
    private BukkitTask preparationTask;

    public MatchWorldPoolManager(Main main, MatchWorldLifecycleManager lifecycleManager,
            MatchWorldEvaluator evaluator) {
        this.main = main;
        this.lifecycleManager = lifecycleManager;
        this.evaluator = evaluator;
        load();
        normalizeLoadedState();
    }

    public void prepareOnStartupIfNeeded() {
        if (!isEnabled() || !main.getConfig().getBoolean(PREPARE_ON_STARTUP, true)) {
            return;
        }
        if (count(MatchWorldPoolEntry.Status.READY) > 0 || isPreparing()) {
            return;
        }

        long delayTicks = Math.max(0, main.getConfig().getInt(PREPARE_STARTUP_DELAY_SECONDS, 10)) * 20L;
        new BukkitRunnable() {
            @Override
            public void run() {
                startBatchPreparation();
            }
        }.runTaskLater(main, delayTicks);
    }

    public boolean isEnabled() {
        return main.getConfig().getBoolean(POOL_ENABLED, false);
    }

    public boolean isPreparing() {
        return preparationTask != null && !preparationTask.isCancelled();
    }

    public void startBatchPreparation() {
        if (!isEnabled() || isPreparing()) {
            return;
        }

        int batchSize = Math.max(1, main.getConfig().getInt(BATCH_SIZE, 10));
        long delayBetweenWorldsTicks = Math.max(1, main.getConfig().getInt(DELAY_BETWEEN_WORLDS_SECONDS, 20)) * 20L;
        main.getLogger().info("Запущена подготовка партии матч-миров: " + batchSize);

        preparationTask = new BukkitRunnable() {
            private int prepared;

            @Override
            public void run() {
                if (prepared >= batchSize) {
                    main.getLogger().info("Подготовка партии матч-миров завершена.");
                    cancel();
                    preparationTask = null;
                    return;
                }

                prepareOneWorld();
                prepared++;
            }
        }.runTaskTimer(main, 0L, delayBetweenWorldsTicks);
    }

    public Optional<MatchWorldCandidate> takeReadyCandidate() {
        if (!isEnabled()) {
            return Optional.empty();
        }

        Optional<MatchWorldPoolEntry> selected = findBestReadyEntry();
        if (selected.isEmpty()) {
            if (main.getConfig().getBoolean(GENERATE_ON_START_IF_EMPTY, false) && !isPreparing()) {
                startBatchPreparation();
            }
            return Optional.empty();
        }

        MatchWorldPoolEntry entry = selected.get().withStatus(MatchWorldPoolEntry.Status.ACTIVE);
        World world = lifecycleManager.loadMatchWorldSet(entry.baseWorldName(), entry.seed());
        if (world == null) {
            entries.put(entry.id(), entry.withStatus(MatchWorldPoolEntry.Status.FAILED));
            save();
            return Optional.empty();
        }

        Location fixedStart = copyLocationToWorld(entry.startLocation(), world);
        entry = new MatchWorldPoolEntry(entry.id(), entry.status(), entry.baseWorldName(), entry.seed(),
                entry.score(), fixedStart, entry.detailMessage(), entry.runnerHintMessage(),
                entry.createdAt());
        world.setSpawnLocation(entry.startLocation());
        entries.put(entry.id(), entry);
        save();
        return Optional.of(entry.toCandidate(world));
    }

    public void releaseActive(String baseWorldName) {
        if (baseWorldName == null) {
            return;
        }
        if (!isPoolWorld(baseWorldName)) {
            main.getLogger().warning("Отказано в releaseActive для мира вне pool namespace: " + baseWorldName);
            return;
        }
        entries.values().stream()
                .filter(entry -> entry.baseWorldName().equals(baseWorldName))
                .findFirst()
                .ifPresent(entry -> {
                    entries.put(entry.id(), entry.withStatus(MatchWorldPoolEntry.Status.USED));
                    save();
                    if (main.getConfig().getBoolean(DELETE_USED_WORLDS, true)) {
                        lifecycleManager.disposeWorldSet(entry.baseWorldName());
                        entries.remove(entry.id());
                        save();
                    }
                });
    }

    public int clear(boolean includeActive) {
        int deleted = 0;
        if (isPreparing()) {
            preparationTask.cancel();
            preparationTask = null;
        }

        for (MatchWorldPoolEntry entry : entries.values().toArray(MatchWorldPoolEntry[]::new)) {
            if (entry.status() == MatchWorldPoolEntry.Status.ACTIVE && !includeActive) {
                continue;
            }
            if (!isPoolWorld(entry.baseWorldName())) {
                main.getLogger().warning("Пропущено удаление мира вне pool namespace: " + entry.baseWorldName());
                entries.put(entry.id(), entry.withStatus(MatchWorldPoolEntry.Status.FAILED));
                continue;
            }
            lifecycleManager.disposeWorldSet(entry.baseWorldName());
            entries.remove(entry.id());
            deleted++;
        }
        save();
        return deleted;
    }

    public PoolStatus status() {
        return new PoolStatus(
                count(MatchWorldPoolEntry.Status.READY),
                count(MatchWorldPoolEntry.Status.PREPARING),
                count(MatchWorldPoolEntry.Status.ACTIVE),
                count(MatchWorldPoolEntry.Status.USED),
                count(MatchWorldPoolEntry.Status.FAILED),
                isPreparing());
    }

    private void prepareOneWorld() {
        String id = "pool_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
        String baseWorldName = resolvePoolPrefix() + id;
        long seed = resolveSeed();
        MatchWorldPoolEntry preparingEntry = new MatchWorldPoolEntry(id, MatchWorldPoolEntry.Status.PREPARING,
                baseWorldName, seed, 0, null, "", "", System.currentTimeMillis());
        entries.put(id, preparingEntry);
        save();

        try {
            World world = lifecycleManager.createMatchWorldSet(baseWorldName, seed);
            if (world == null) {
                entries.put(id, preparingEntry.withStatus(MatchWorldPoolEntry.Status.FAILED));
                save();
                return;
            }

            MatchWorldCandidate candidate = evaluator.evaluate(world, seed);
            world.setSpawnLocation(candidate.startLocation());
            entries.put(id, new MatchWorldPoolEntry(id, MatchWorldPoolEntry.Status.READY, baseWorldName, seed,
                    candidate.score(), candidate.startLocation(), candidate.detailMessage(),
                    candidate.runnerHintMessage(), System.currentTimeMillis()));
            save();
            if (main.getConfig().getBoolean(UNLOAD_READY_WORLDS, true)) {
                preloadBestReadyWorldSet();
            }
            main.getLogger().info("Подготовлен матч-мир " + baseWorldName + " score=" + candidate.score());
        } catch (RuntimeException exception) {
            main.getLogger().log(Level.SEVERE, "Не удалось подготовить матч-мир " + baseWorldName, exception);
            entries.put(id, preparingEntry.withStatus(MatchWorldPoolEntry.Status.FAILED));
            lifecycleManager.disposeWorldSet(baseWorldName);
            save();
        }
    }

    private long resolveSeed() {
        List<Long> fixedSeeds = main.getConfig().getLongList(FIXED_SEEDS);
        if (!fixedSeeds.isEmpty()) {
            return fixedSeeds.get(entries.size() % fixedSeeds.size());
        }
        return java.util.concurrent.ThreadLocalRandom.current().nextLong();
    }

    private String resolvePoolPrefix() {
        return main.getConfig().getString(WORLD_PREFIX, "manhunt_pool_");
    }

    private boolean isPoolWorld(String baseWorldName) {
        String prefix = resolvePoolPrefix();
        return baseWorldName != null && !prefix.isBlank() && baseWorldName.startsWith(prefix);
    }

    private void preloadBestReadyWorldSet() {
        Optional<MatchWorldPoolEntry> bestReady = findBestReadyEntry();
        for (MatchWorldPoolEntry entry : entries.values()) {
            if (entry.status() != MatchWorldPoolEntry.Status.READY) {
                continue;
            }
            if (bestReady.isPresent() && bestReady.get().id().equals(entry.id())) {
                lifecycleManager.loadMatchWorldSet(entry.baseWorldName(), entry.seed());
            } else {
                lifecycleManager.unloadWorldSet(entry.baseWorldName());
            }
        }
    }

    private Optional<MatchWorldPoolEntry> findBestReadyEntry() {
        return entries.values().stream()
                .filter(entry -> entry.status() == MatchWorldPoolEntry.Status.READY)
                .max(Comparator.comparingInt(MatchWorldPoolEntry::score)
                        .thenComparingLong(MatchWorldPoolEntry::createdAt));
    }

    private Location copyLocationToWorld(Location source, World world) {
        return new Location(world, source.getX(), source.getY(), source.getZ(), source.getYaw(), source.getPitch());
    }

    private int count(MatchWorldPoolEntry.Status status) {
        return (int) entries.values().stream()
                .filter(entry -> entry.status() == status)
                .count();
    }

    private void normalizeLoadedState() {
        boolean changed = false;
        for (MatchWorldPoolEntry entry : entries.values().toArray(MatchWorldPoolEntry[]::new)) {
            if (entry.status() == MatchWorldPoolEntry.Status.PREPARING) {
                entries.put(entry.id(), entry.withStatus(MatchWorldPoolEntry.Status.FAILED));
                changed = true;
            } else if (entry.status() == MatchWorldPoolEntry.Status.ACTIVE) {
                entries.put(entry.id(), entry.withStatus(MatchWorldPoolEntry.Status.USED));
                changed = true;
            }
        }
        if (changed) {
            save();
        }
    }

    private void load() {
        File file = poolFile();
        if (!file.exists()) {
            return;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = yaml.getConfigurationSection("entries");
        if (section == null) {
            return;
        }
        for (String id : section.getKeys(false)) {
            ConfigurationSection entrySection = section.getConfigurationSection(id);
            if (entrySection != null) {
                entries.put(id, MatchWorldPoolEntry.deserialize(id, entrySection));
            }
        }
    }

    private void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (MatchWorldPoolEntry entry : entries.values()) {
            yaml.set("entries." + entry.id(), entry.serialize());
        }
        try {
            yaml.save(poolFile());
        } catch (IOException exception) {
            main.getLogger().log(Level.SEVERE, "Не удалось сохранить match-world-pool.yml", exception);
        }
    }

    private File poolFile() {
        return new File(main.getDataFolder(), POOL_FILE_NAME);
    }

    public record PoolStatus(int ready, int preparing, int active, int used, int failed, boolean batchRunning) {
    }
}
