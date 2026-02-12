package me.matistan05.minecraftmanhunt;

import me.matistan05.minecraftmanhunt.abilities.core.AbilitiesManager;
import me.matistan05.minecraftmanhunt.abilities.gui.AbilitiesMenu;
import me.matistan05.minecraftmanhunt.abilities.listeners.AbilitiesController;
import me.matistan05.minecraftmanhunt.commands.ManhuntCommand;
import me.matistan05.minecraftmanhunt.commands.ManhuntCompleter;
import me.matistan05.minecraftmanhunt.listeners.*;
import org.bukkit.plugin.java.JavaPlugin;

import static me.matistan05.minecraftmanhunt.commands.ManhuntCommand.inGame;
import static me.matistan05.minecraftmanhunt.commands.ManhuntCommand.waitingForStart;
import static me.matistan05.minecraftmanhunt.commands.ManhuntCommand.reset;

public final class Main extends JavaPlugin {

    public void onEnable() {
        saveDefaultConfig();

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
        JavaPlugin.getPlugin(this.getClass()).getLogger().info(
                "\n\n*********************************************************\n" +
                        "Thank you for using this plugin! <3\n" +
                        "Author: Matistan\n" +
                        "Co-Author: hetashi (refining this plugin now)\n" +
                        "If you enjoy this plugin, please rate it on spigotmc.org:\n" +
                        "https://www.spigotmc.org/resources/manhunt.109010/\n" +
                        "*********************************************************\n");
    }

    @Override
    public void onDisable() {
        if (inGame || waitingForStart) {
            reset();
        }
    }
}
