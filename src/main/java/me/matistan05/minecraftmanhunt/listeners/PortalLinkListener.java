package me.matistan05.minecraftmanhunt.listeners;

import io.papermc.paper.event.entity.EntityPortalReadyEvent;
import me.matistan05.minecraftmanhunt.Main;
import org.bukkit.Bukkit;
import org.bukkit.PortalType;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class PortalLinkListener implements Listener {

    private final Main main;

    public PortalLinkListener(Main main) {
        this.main = main;
        Bukkit.getPluginManager().registerEvents(this, main);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPortalReady(EntityPortalReadyEvent event) {
        if (!main.getConfig().getBoolean("match-worlds.enabled", false)) {
            return;
        }

        World sourceWorld = event.getEntity().getWorld();
        World.Environment targetEnvironment = resolveTargetEnvironment(sourceWorld.getEnvironment(), event.getPortalType());
        if (targetEnvironment == null) {
            return;
        }

        World targetWorld = main.getMatchWorldManager().getLinkedWorld(sourceWorld, targetEnvironment);
        if (targetWorld != null) {
            event.setTargetWorld(targetWorld);
        }
    }

    private World.Environment resolveTargetEnvironment(World.Environment sourceEnvironment, PortalType portalType) {
        if (portalType == PortalType.NETHER) {
            return sourceEnvironment == World.Environment.NETHER
                    ? World.Environment.NORMAL
                    : World.Environment.NETHER;
        }
        if (portalType == PortalType.ENDER) {
            return sourceEnvironment == World.Environment.THE_END
                    ? World.Environment.NORMAL
                    : World.Environment.THE_END;
        }
        return null;
    }
}
