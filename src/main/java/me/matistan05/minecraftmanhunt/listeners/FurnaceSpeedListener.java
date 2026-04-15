package me.matistan05.minecraftmanhunt.listeners;

import me.matistan05.minecraftmanhunt.Main;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceStartSmeltEvent;

import static me.matistan05.minecraftmanhunt.commands.ManhuntCommand.inGame;

public class FurnaceSpeedListener implements Listener {
    private final Main main;

    public FurnaceSpeedListener(Main main) {
        this.main = main;
        Bukkit.getPluginManager().registerEvents(this, main);
    }

    @EventHandler
    public void onFurnaceStartSmelt(FurnaceStartSmeltEvent e) {
        if (!inGame || !main.getConfig().getBoolean("gameplay.fast-furnace-speed", true)) {
            return;
        }

        int totalCookTime = e.getTotalCookTime();
        if (totalCookTime <= 1) {
            return;
        }

        e.setTotalCookTime(Math.max(1, totalCookTime / 2));
    }
}
