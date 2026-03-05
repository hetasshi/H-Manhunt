package me.matistan05.minecraftmanhunt;

import me.matistan05.minecraftmanhunt.abilities.core.AbilitiesManager;
import me.matistan05.minecraftmanhunt.abilities.gui.AbilitiesMenu;
import me.matistan05.minecraftmanhunt.abilities.listeners.AbilitiesController;
import me.matistan05.minecraftmanhunt.commands.ManhuntCommand;
import me.matistan05.minecraftmanhunt.commands.ManhuntCompleter;
import me.matistan05.minecraftmanhunt.managers.UpdateManager;
import me.matistan05.minecraftmanhunt.listeners.*;
import org.bukkit.plugin.java.JavaPlugin;

import static me.matistan05.minecraftmanhunt.commands.ManhuntCommand.inGame;
import static me.matistan05.minecraftmanhunt.commands.ManhuntCommand.waitingForStart;
import static me.matistan05.minecraftmanhunt.commands.ManhuntCommand.reset;

public final class Main extends JavaPlugin {
    private UpdateManager updateManager;

    public void onEnable() {
        saveDefaultConfig();
        updateManager = new UpdateManager(this);

        AbilitiesManager abilitiesManager = new AbilitiesManager(this);
        AbilitiesMenu abilitiesMenu = new AbilitiesMenu(this);
        getServer().getPluginManager().registerEvents(new AbilitiesController(this, abilitiesManager, abilitiesMenu),
                this);
        getServer().getPluginCommand("manhunt").setExecutor(new ManhuntCommand(this));
        getCommand("manhunt").setTabCompleter(new ManhuntCompleter(this));
        new DeathListener(this);
        new AdvancementListener(this);
        new InteractListener(this);
        new RespawnListener(this);
        new DropListener(this);
        new MoveListener(this);
        new MenuListener(this);
        new DisconnectListener(this);
        new DamageListener(this);
        new FurnaceSpeedListener(this);

        String o = net.md_5.bungee.api.ChatColor.of("#eb5e28").toString();
        String w = net.md_5.bungee.api.ChatColor.of("#fffcf2").toString();
        String g = net.md_5.bungee.api.ChatColor.of("#ccc5b9").toString();
        boolean autoUpdaterEnabled = getConfig().getBoolean("update.enabled", true)
                && getConfig().getBoolean("update.checkOnStartup", true);
        String autoUpdaterStatus = autoUpdaterEnabled ? w + "enabled" : g + "disabled";
        getServer().getConsoleSender().sendMessage(new String[] {
                "",
                o + "=========================================================",
                o + "Thank you for using H-Manhunt! " + net.md_5.bungee.api.ChatColor.RED + "❤",
                o + "Original Author: " + w + "Matistan05",
                o + "Fork & Maintainer: " + w + "hetashi",
                o + "Version: " + w + getPluginMeta().getVersion(),
                o + "Updates: " + w + "GitHub Releases " + g + "(auto-updater: " + autoUpdaterStatus + g + ")",
                o + "=========================================================",
                ""
        });

        updateManager.checkOnStartup();
    }

    @Override
    public void onDisable() {
        if (inGame || waitingForStart) {
            reset();
        }
    }

    public UpdateManager getUpdateManager() {
        return updateManager;
    }
}
