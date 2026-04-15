package me.matistan05.minecraftmanhunt.listeners;

import me.matistan05.minecraftmanhunt.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

import static me.matistan05.minecraftmanhunt.commands.ManhuntCommand.*;

public class RespawnListener implements Listener {
    private final Main main;

    public RespawnListener(Main main) {
        this.main = main;
        Bukkit.getPluginManager().registerEvents(this, main);
    }

    @EventHandler
    public void RespawnEvent(PlayerRespawnEvent e) {
        if (!(inGame || waitingForStart) || !isInGame(e.getPlayer().getName())) {
            return;
        }

        Location respawnLocation = resolveMatchRespawnLocation();
        if (respawnLocation != null) {
            e.setRespawnLocation(respawnLocation);
        }

        if (isHunter(e.getPlayer().getName())) {
            if (waypointManager.getCompassSlot(e.getPlayer()) == -1) {
                e.getPlayer().getInventory().setItem(8, waypointManager.getCompass());
            }
        }
    }

    private Location resolveMatchRespawnLocation() {
        if (main.getConfig().getBoolean("match-worlds.enabled", false)) {
            for (String worldName : main.getMatchWorldManager().getActiveWorldNames()) {
                World world = Bukkit.getWorld(worldName);
                if (world != null && world.getEnvironment() == World.Environment.NORMAL) {
                    return world.getSpawnLocation();
                }
            }
        }

        if (startLocation != null) {
            return startLocation.clone();
        }

        return null;
    }
}
