package me.matistan05.minecraftmanhunt.abilities.core;

import me.matistan05.minecraftmanhunt.Main;
import me.matistan05.minecraftmanhunt.classes.Hunter;
import me.matistan05.minecraftmanhunt.classes.Speedrunner;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import static me.matistan05.minecraftmanhunt.commands.ManhuntCommand.*;

public class AbilitiesManager {
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

        Location newLoc = calculateWarpLocation(p.getLocation(), targetLoc, maxDist);
        newLoc = findSafeLocation(newLoc);

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

    private Location calculateWarpLocation(Location hLoc, Location tLoc, int maxDist) {
        Vector dir = tLoc.toVector().subtract(hLoc.toVector()).normalize();
        return tLoc.clone().subtract(dir.multiply(maxDist));
    }

    private Location findSafeLocation(Location loc) {
        Location safe = loc.clone();
        while ((!safe.getBlock().getType().isAir() || !safe.clone().add(0, 1, 0).getBlock().getType().isAir()
                || safe.getBlock().isLiquid())
                && safe.getY() < 319) {
            safe.add(0, 1, 0);
        }
        while (safe.clone().add(0, -1, 0).getBlock().getType().isAir() && safe.getY() > -64) {
            safe.add(0, -1, 0);
        }
        return safe;
    }
}
