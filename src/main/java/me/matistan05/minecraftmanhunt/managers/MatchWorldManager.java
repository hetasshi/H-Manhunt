package me.matistan05.minecraftmanhunt.managers;

import me.matistan05.minecraftmanhunt.Main;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class MatchWorldManager {
    private static final int MAX_LIVE_EVALUATION_SCORE = 180;

    private final Main main;
    private final MatchWorldLifecycleManager lifecycleManager;
    private final MatchWorldEvaluator evaluator;
    private MatchWorldSession activeSession;

    public MatchWorldManager(Main main) {
        this.main = main;
        this.lifecycleManager = new MatchWorldLifecycleManager(main);
        this.evaluator = new MatchWorldEvaluator(main);
    }

    public PreparedMatchWorld prepareMatchWorld() {
        if (!main.getConfig().getBoolean("matchWorlds.enabled", false)) {
            return PreparedMatchWorld.disabled();
        }

        lifecycleManager.cleanupUnusedAutoWorlds(activeSession == null ? null : activeSession.baseWorldName());

        int maxAttempts = Math.max(1, main.getConfig().getInt("matchWorlds.autoGenerate.maxAttempts", 4));
        int minScore = Math.min(
                main.getConfig().getInt("matchWorlds.autoGenerate.minScoreToAccept", 90),
                MAX_LIVE_EVALUATION_SCORE);
        boolean fallbackToBest = main.getConfig().getBoolean(
                "matchWorlds.autoGenerate.acceptBestCandidateIfThresholdMissed", true);

        MatchWorldCandidate bestCandidate = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            String worldName = nextBaseWorldName();
            long seed = resolveSeed(attempt);
            var world = lifecycleManager.createMatchWorldSet(worldName, seed);
            if (world == null) {
                continue;
            }

            MatchWorldCandidate candidate = evaluator.evaluate(world, seed);
            if (bestCandidate == null || candidate.score() > bestCandidate.score()) {
                if (bestCandidate != null) {
                    lifecycleManager.disposeWorldSet(bestCandidate.world().getName());
                }
                bestCandidate = candidate;
            } else {
                lifecycleManager.disposeWorldSet(candidate.world().getName());
            }

            if (candidate.score() >= minScore) {
                return activateCandidate(candidate);
            }
        }

        if (bestCandidate != null && fallbackToBest) {
            return activateCandidate(bestCandidate);
        }
        return PreparedMatchWorld.failure("Не удалось подобрать достаточно хороший мир для старта.");
    }

    public void resetActiveMatchWorld() {
        if (!main.getConfig().getBoolean("matchWorlds.enabled", false)) {
            return;
        }
        if (activeSession != null) {
            lifecycleManager.disposeWorldSet(activeSession.baseWorldName());
            activeSession = null;
        }
        lifecycleManager.cleanupUnusedAutoWorlds(null);
    }

    public String getRunnerHintMessage() {
        return activeSession == null ? null : activeSession.runnerHintMessage();
    }

    public Set<String> getActiveWorldNames() {
        return activeSession == null ? Set.of() : activeSession.worldNames();
    }

    public World getLinkedWorld(World sourceWorld, World.Environment targetEnvironment) {
        if (sourceWorld == null || activeSession == null || !activeSession.worldNames().contains(sourceWorld.getName())) {
            return null;
        }

        String targetWorldName = switch (targetEnvironment) {
            case NORMAL -> activeSession.baseWorldName();
            case NETHER -> activeSession.baseWorldName() + "_nether";
            case THE_END -> activeSession.baseWorldName() + "_the_end";
            default -> null;
        };

        return targetWorldName == null ? null : Bukkit.getWorld(targetWorldName);
    }

    private PreparedMatchWorld activateCandidate(MatchWorldCandidate candidate) {
        activeSession = new MatchWorldSession(
                candidate.world().getName(),
                lifecycleManager.getWorldNamesForBase(candidate.world().getName()),
                candidate.seed(),
                candidate.detailMessage(),
                candidate.runnerHintMessage());
        lifecycleManager.cleanupUnusedAutoWorlds(activeSession.baseWorldName());
        return PreparedMatchWorld.success(candidate);
    }

    private long resolveSeed(int attempt) {
        List<Long> fixedSeeds = main.getConfig().getLongList("matchWorlds.autoGenerate.fixedSeeds");
        if (!fixedSeeds.isEmpty()) {
            return fixedSeeds.get((attempt - 1) % fixedSeeds.size());
        }
        return ThreadLocalRandom.current().nextLong();
    }

    private String nextBaseWorldName() {
        String prefix = main.getConfig().getString("matchWorlds.autoGenerate.worldPrefix", "manhunt_match_");
        return prefix + System.currentTimeMillis();
    }

    public record PreparedMatchWorld(boolean enabled, boolean success, MatchWorldCandidate candidate, String detailMessage) {
        public static PreparedMatchWorld disabled() {
            return new PreparedMatchWorld(false, true, null, null);
        }

        public static PreparedMatchWorld success(MatchWorldCandidate candidate) {
            return new PreparedMatchWorld(true, true, candidate, candidate.detailMessage());
        }

        public static PreparedMatchWorld failure(String detailMessage) {
            return new PreparedMatchWorld(true, false, null, detailMessage);
        }
    }
}
