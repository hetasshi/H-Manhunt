package me.matistan05.minecraftmanhunt.managers;

import java.util.Set;

public record MatchWorldSession(
        String baseWorldName,
        Set<String> worldNames,
        long seed,
        String detailMessage,
        String runnerHintMessage) {
}
