package me.matistan05.minecraftmanhunt.managers;

import org.bukkit.Location;
import org.bukkit.World;

public record MatchWorldCandidate(
        World world,
        Location startLocation,
        long seed,
        int score,
        String detailMessage,
        String runnerHintMessage) {
}
