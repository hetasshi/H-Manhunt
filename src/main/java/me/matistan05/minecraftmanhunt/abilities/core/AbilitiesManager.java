package me.matistan05.minecraftmanhunt.abilities.core;

import me.matistan05.minecraftmanhunt.Main;
import me.matistan05.minecraftmanhunt.classes.Hunter;
import me.matistan05.minecraftmanhunt.classes.Speedrunner;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import static me.matistan05.minecraftmanhunt.commands.ManhuntCommand.*;

public class AbilitiesManager {
    private static final int[] ANGLE_OFFSETS = {0, 5, -5, 10, -10, 15, -15, 20, -20, 30, -30, 40, -40, 55, -55, 70,
            -70, 80, -80};
    private static final int[] RADIUS_OFFSETS = {0, -4, 4, -8, 8, -12, 12};
    private static final int[] VERTICAL_SEARCH_RANGES = {4, 8, 14, 22};

    private final Main main;

    public AbilitiesManager(Main main) {
        this.main = main;
    }

    public void useWarpShadows(Player p) {
        Hunter hunter = getHunter(p.getName());
        if (hunter == null)
            return;

        long now = System.currentTimeMillis();
        int cdSeconds = main.getConfig().getInt("warpShadowsCooldown");
        MiniMessage mm = MiniMessage.miniMessage();
        if (now - hunter.getLastWarpShadows() < cdSeconds * 1000L) {
            long remaining = (cdSeconds * 1000L - (now - hunter.getLastWarpShadows())) / 1000;
            p.sendMessage(mm.deserialize("<red>Способность на перезарядке! Осталось: <white>" + remaining + "с"));
            return;
        }

        Location targetLoc = findTargetLocation(p, hunter);
        if (targetLoc == null) {
            p.sendMessage(mm.deserialize("<red>Цель не найдена!"));
            return;
        }

        double distance = p.getLocation().distance(targetLoc);
        int maxDist = main.getConfig().getInt("warpShadowsMaxDistance");
        int buffer = main.getConfig().getInt("warpShadowsBufferZone");

        if (distance <= (maxDist + buffer)) {
            p.sendMessage(mm.deserialize(
                    "<gradient:#ffaa00:#ffff55>Вы уже близко!</gradient>"));
            return;
        }

        Location newLoc = findBestWarpLocation(p.getLocation(), targetLoc, maxDist);
        if (newLoc == null) {
            p.sendMessage(mm.deserialize("<red>Безопасная точка для варпа не найдена!"));
            return;
        }

        p.teleport(newLoc);
        hunter.setLastWarpShadows(now);
        p.sendMessage(mm.deserialize("<gradient:#aa00ff:#ff55ff><bold>ВАРП ТЕНИ ИСПОЛЬЗОВАН!</bold></gradient>"));
        p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        p.closeInventory();
    }

    private Location findTargetLocation(Player hunterPlayer, Hunter hunter) {
        if (hunter.getCompassMode() == 0) {
            double minDist = Double.MAX_VALUE;
            Location closest = null;
            for (Speedrunner sObj : speedrunners) {
                Player s = Bukkit.getPlayerExact(sObj.getName());
                Location potential = null;
                if (s != null && s.getWorld().equals(hunterPlayer.getWorld())) {
                    potential = s.getLocation();
                } else if (main.getConfig().getBoolean("trackPortals")) {
                    potential = getLastKnownLocation(sObj, hunterPlayer.getWorld().getEnvironment());
                }
                if (potential != null && hunterPlayer.getWorld().equals(potential.getWorld())) {
                    double d = hunterPlayer.getLocation().distance(potential);
                    if (d < minDist) {
                        minDist = d;
                        closest = potential;
                    }
                }
            }
            return closest;
        } else {
            String targetName = hunter.getWhichSpeedrunner();
            if (!isSpeedrunner(targetName)) {
                if (!speedrunners.isEmpty()) {
                    targetName = speedrunners.get(0).getName();
                } else {
                    return null;
                }
            }

            Player target = Bukkit.getPlayerExact(targetName);
            Speedrunner sObj = getSpeedrunner(targetName);

            if (target != null && target.getWorld().equals(hunterPlayer.getWorld())) {
                return target.getLocation();
            } else if (main.getConfig().getBoolean("trackPortals")) {
                return getLastKnownLocation(sObj, hunterPlayer.getWorld().getEnvironment());
            }
        }
        return null;
    }

    private Location getLastKnownLocation(Speedrunner speedrunner, World.Environment env) {
        if (speedrunner == null)
            return null;
        if (env == World.Environment.NORMAL)
            return speedrunner.getLocWorld();
        if (env == World.Environment.NETHER)
            return speedrunner.getLocNether();
        if (env == World.Environment.THE_END)
            return speedrunner.getLocTheEnd();
        return null;
    }

    private Location findBestWarpLocation(Location hunterLoc, Location targetLoc, int maxDist) {
        if (hunterLoc.getWorld() == null || targetLoc.getWorld() == null
                || !hunterLoc.getWorld().equals(targetLoc.getWorld())) {
            return null;
        }

        Vector hunterToTarget = targetLoc.toVector().subtract(hunterLoc.toVector()).setY(0);
        if (hunterToTarget.lengthSquared() == 0) {
            return null;
        }

        Vector preferredDir = hunterToTarget.clone().normalize().multiply(-1);
        Vector hunterSide = hunterLoc.toVector().subtract(targetLoc.toVector()).setY(0);
        if (hunterSide.lengthSquared() > 0) {
            hunterSide.normalize();
        } else {
            hunterSide = preferredDir.clone();
        }

        Location best = null;
        double bestScore = Double.MAX_VALUE;

        for (int angleOffset : ANGLE_OFFSETS) {
            double radians = Math.toRadians(angleOffset);
            for (int radiusOffset : RADIUS_OFFSETS) {
                int radius = Math.max(4, maxDist + radiusOffset);
                Vector dir = preferredDir.clone().rotateAroundY(radians);
                Location candidateXZ = targetLoc.clone().add(dir.multiply(radius));

                if (isAheadOfTarget(candidateXZ, targetLoc, hunterSide)) {
                    continue;
                }

                Location safeCandidate = findSafeLocationNearY(candidateXZ);
                if (safeCandidate == null || isAheadOfTarget(safeCandidate, targetLoc, hunterSide)) {
                    continue;
                }

                double yShift = Math.abs(safeCandidate.getY() - candidateXZ.getY());
                double waterPenalty = isWater(safeCandidate.getBlock().getType()) ? 5.0 : 0.0;
                double score = Math.abs(angleOffset) * 1000.0 + Math.abs(radiusOffset) * 20.0 + yShift + waterPenalty;

                if (score < bestScore) {
                    bestScore = score;
                    best = safeCandidate;
                }
            }
        }

        return best;
    }

    private Location findSafeLocationNearY(Location base) {
        World world = base.getWorld();
        if (world == null) {
            return null;
        }

        int minY = world.getMinHeight();
        int maxY = world.getMaxHeight() - 1;
        int baseY = base.getBlockY();

        for (int range : VERTICAL_SEARCH_RANGES) {
            for (int offset = 0; offset <= range; offset++) {
                Location positive = tryLocationAtY(base, baseY + offset, minY, maxY);
                if (positive != null) {
                    return positive;
                }

                if (offset == 0) {
                    continue;
                }

                Location negative = tryLocationAtY(base, baseY - offset, minY, maxY);
                if (negative != null) {
                    return negative;
                }
            }
        }

        return null;
    }

    private Location tryLocationAtY(Location base, int y, int minY, int maxY) {
        if (y < minY || y > maxY) {
            return null;
        }

        Location candidate = base.clone();
        candidate.setY(y);

        if (isSafeWarpSpot(candidate)) {
            return candidate;
        }

        return null;
    }

    private boolean isSafeWarpSpot(Location loc) {
        Material feet = loc.getBlock().getType();
        Material head = loc.clone().add(0, 1, 0).getBlock().getType();
        Material below = loc.clone().add(0, -1, 0).getBlock().getType();

        if (isLava(feet) || isLava(head) || isLava(below)) {
            return false;
        }

        if (!isPassableForWarp(feet) || !isPassableForWarp(head)) {
            return false;
        }

        if (isWater(feet)) {
            return true;
        }

        return !below.isAir() && !isWater(below);
    }

    private boolean isPassableForWarp(Material material) {
        return material.isAir() || isWater(material);
    }

    private boolean isWater(Material material) {
        return material == Material.WATER;
    }

    private boolean isLava(Material material) {
        return material == Material.LAVA;
    }

    private boolean isAheadOfTarget(Location candidate, Location target, Vector hunterSide) {
        Vector candidateSide = candidate.toVector().subtract(target.toVector()).setY(0);
        if (candidateSide.lengthSquared() == 0) {
            return false;
        }
        return hunterSide.dot(candidateSide) <= 0;
    }
}
