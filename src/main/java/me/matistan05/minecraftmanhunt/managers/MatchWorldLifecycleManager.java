package me.matistan05.minecraftmanhunt.managers;

import me.matistan05.minecraftmanhunt.Main;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

public class MatchWorldLifecycleManager {

    private final Main main;

    public MatchWorldLifecycleManager(Main main) {
        this.main = main;
    }

    public World createMatchWorldSet(String baseWorldName, long seed) {
        World overworld = createWorld(baseWorldName, seed, World.Environment.NORMAL);
        if (overworld == null) {
            return null;
        }

        createWorld(baseWorldName + "_nether", seed, World.Environment.NETHER);
        createWorld(baseWorldName + "_the_end", seed, World.Environment.THE_END);
        return overworld;
    }

    public World loadMatchWorldSet(String baseWorldName, long seed) {
        return createMatchWorldSet(baseWorldName, seed);
    }

    private World createWorld(String worldName, long seed, World.Environment environment) {
        WorldCreator creator = WorldCreator.name(worldName)
                .generateStructures(true)
                .environment(environment)
                .seed(seed);

        if (environment == World.Environment.NORMAL) {
            creator.type(WorldType.NORMAL);
        }

        return creator.createWorld();
    }

    public void cleanupUnusedAutoWorlds(String activeBaseWorldName) {
        String prefix = main.getConfig().getString("match-worlds.auto-generate.world-prefix", "manhunt_match_");
        int keepLatest = Math.max(1, main.getConfig().getInt("match-worlds.auto-generate.keep-latest-worlds", 2));

        List<String> autoWorldBases = Bukkit.getWorlds().stream()
                .map(World::getName)
                .filter(worldName -> worldName.startsWith(prefix))
                .map(this::toBaseWorldName)
                .distinct()
                .sorted(Comparator.reverseOrder())
                .toList();

        int kept = 0;
        for (String baseWorldName : autoWorldBases) {
            boolean isActive = activeBaseWorldName != null && activeBaseWorldName.equalsIgnoreCase(baseWorldName);
            if (isActive || kept < keepLatest) {
                kept++;
                continue;
            }
            if (hasPlayersInWorldSet(baseWorldName)) {
                continue;
            }
            disposeWorldSet(baseWorldName);
        }
    }

    public void disposeWorldSet(String baseWorldName) {
        for (String worldName : getWorldNamesForBase(baseWorldName)) {
            disposeWorld(worldName);
        }
    }

    public void unloadWorldSet(String baseWorldName) {
        for (String worldName : getWorldNamesForBase(baseWorldName)) {
            World world = Bukkit.getWorld(worldName);
            if (world != null && world.getPlayers().isEmpty()) {
                Bukkit.unloadWorld(world, true);
            }
        }
    }

    public Set<String> getWorldNamesForBase(String baseWorldName) {
        Set<String> worldNames = new LinkedHashSet<>();
        worldNames.add(baseWorldName);
        worldNames.add(baseWorldName + "_nether");
        worldNames.add(baseWorldName + "_the_end");
        return worldNames;
    }

    private boolean hasPlayersInWorldSet(String baseWorldName) {
        return getWorldNamesForBase(baseWorldName).stream()
                .map(Bukkit::getWorld)
                .filter(Objects::nonNull)
                .anyMatch(world -> !world.getPlayers().isEmpty());
    }

    private String toBaseWorldName(String worldName) {
        if (worldName.endsWith("_nether")) {
            return worldName.substring(0, worldName.length() - "_nether".length());
        }
        if (worldName.endsWith("_the_end")) {
            return worldName.substring(0, worldName.length() - "_the_end".length());
        }
        return worldName;
    }

    private void disposeWorld(World world) {
        if (world == null) {
            return;
        }
        World fallbackWorld = getFallbackWorld(world);
        if (fallbackWorld != null) {
            for (Player player : world.getPlayers()) {
                player.teleport(fallbackWorld.getSpawnLocation());
            }
        }
        Bukkit.unloadWorld(world, false);
        deleteWorldFolder(world.getWorldFolder().toPath());
    }

    public void disposeWorld(String worldName) {
        World loadedWorld = Bukkit.getWorld(worldName);
        if (loadedWorld != null) {
            disposeWorld(loadedWorld);
            return;
        }
        Path worldPath = Bukkit.getWorldContainer().toPath().resolve(worldName);
        deleteWorldFolder(worldPath);
    }

    private World getFallbackWorld(World currentWorld) {
        return Bukkit.getWorlds().stream()
                .filter(world -> !Objects.equals(world.getUID(), currentWorld.getUID()))
                .findFirst()
                .orElse(null);
    }

    private void deleteWorldFolder(Path worldPath) {
        if (!Files.exists(worldPath)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(worldPath)) {
            walk.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignored) {
                }
            });
        } catch (IOException ignored) {
        }
    }
}
