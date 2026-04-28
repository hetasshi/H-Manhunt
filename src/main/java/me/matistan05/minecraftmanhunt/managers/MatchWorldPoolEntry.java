package me.matistan05.minecraftmanhunt.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.LinkedHashMap;
import java.util.Map;

public record MatchWorldPoolEntry(
        String id,
        Status status,
        String baseWorldName,
        long seed,
        int score,
        Location startLocation,
        String detailMessage,
        String runnerHintMessage,
        long createdAt) {

    public enum Status {
        PREPARING,
        READY,
        ACTIVE,
        USED,
        FAILED
    }

    public MatchWorldPoolEntry withStatus(Status status) {
        return new MatchWorldPoolEntry(id, status, baseWorldName, seed, score, startLocation, detailMessage,
                runnerHintMessage, createdAt);
    }

    public MatchWorldCandidate toCandidate(World world) {
        return new MatchWorldCandidate(world, startLocation.clone(), seed, score, detailMessage, runnerHintMessage);
    }

    public Map<String, Object> serialize() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", status.name());
        data.put("base-world", baseWorldName);
        data.put("seed", seed);
        data.put("score", score);
        data.put("detail-message", detailMessage);
        data.put("runner-hint-message", runnerHintMessage);
        data.put("created-at", createdAt);

        if (startLocation != null) {
            data.put("start", serializeStartLocation());
        }
        return data;
    }

    public static MatchWorldPoolEntry deserialize(String id, ConfigurationSection section) {
        Status status = parseStatus(section.getString("status", Status.FAILED.name()));
        String baseWorldName = section.getString("base-world", id);
        long seed = section.getLong("seed");
        int score = section.getInt("score");
        String detailMessage = section.getString("detail-message", "");
        String runnerHintMessage = section.getString("runner-hint-message", "");
        long createdAt = section.getLong("created-at", System.currentTimeMillis());

        World world = Bukkit.getWorld(baseWorldName);
        Location startLocation = deserializeStartLocation(section.getConfigurationSection("start"), baseWorldName,
                world);

        return new MatchWorldPoolEntry(id, status, baseWorldName, seed, score, startLocation, detailMessage,
                runnerHintMessage, createdAt);
    }

    private Map<String, Object> serializeStartLocation() {
        Map<String, Object> start = new LinkedHashMap<>();
        start.put("world", startLocation.getWorld() == null ? baseWorldName : startLocation.getWorld().getName());
        start.put("x", startLocation.getX());
        start.put("y", startLocation.getY());
        start.put("z", startLocation.getZ());
        start.put("yaw", startLocation.getYaw());
        start.put("pitch", startLocation.getPitch());
        return start;
    }

    private static Location deserializeStartLocation(ConfigurationSection section, String baseWorldName,
            World fallbackWorld) {
        if (section == null) {
            return new Location(fallbackWorld, 0.5, 80, 0.5);
        }

        String startWorldName = section.getString("world", baseWorldName);
        World startWorld = Bukkit.getWorld(startWorldName);
        return new Location(
                startWorld,
                section.getDouble("x", 0.5),
                section.getDouble("y", 80),
                section.getDouble("z", 0.5),
                (float) section.getDouble("yaw", 0.0),
                (float) section.getDouble("pitch", 0.0));
    }

    private static Status parseStatus(String value) {
        try {
            return Status.valueOf(value);
        } catch (IllegalArgumentException exception) {
            return Status.FAILED;
        }
    }
}
