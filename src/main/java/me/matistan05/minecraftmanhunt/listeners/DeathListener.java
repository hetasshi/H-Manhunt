package me.matistan05.minecraftmanhunt.listeners;

import me.matistan05.minecraftmanhunt.Main;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import static me.matistan05.minecraftmanhunt.commands.ManhuntCommand.*;

public class DeathListener implements Listener {
    Main main;

    public DeathListener(Main main) {
        this.main = main;
        Bukkit.getPluginManager().registerEvents(this, main);
    }

    @EventHandler
    public void DeathEvent(PlayerDeathEvent e) {
        if (!inGame && !waitingForStart)
            return;
        Player p = e.getEntity();
        if (isHunter(p.getName())) {
            e.getDrops().removeIf(waypointManager::isCompass);
        } else if (isSpeedrunner(p.getName())) {
            MiniMessage mm = MiniMessage.miniMessage();
            getSpeedrunner(p.getName()).setLives(getSpeedrunner(p.getName()).getLives() - 1);
            int lives = getSpeedrunner(p.getName()).getLives();
            if (lives >= 1) {
                playersMessage(mm.deserialize("<gray>Спидраннер <gradient:#ff4444:#ffaaaa>" + p.getName()
                        + "</gradient> погиб! Осталось жизней: <white>" + lives + "</white>"));
            } else {
                if (speedrunners.size() == 1) {
                    playersMessage(mm.deserialize("<gradient:#aa0000:#ff0000><bold>Последний спидраннер " + p.getName()
                            + " погиб!</bold></gradient>"));
                    playersMessage(
                            mm.deserialize("<gradient:#ff4444:#ffffff><bold>Охотники победили!</bold></gradient>"));
                    playersTitle(mm.deserialize("<gradient:#aa0000:#ff0000><bold>ПОБЕДА ОХОТНИКОВ!</bold></gradient>"));
                    reset();
                } else {
                    if (main.getConfig().getBoolean("spectatorAfterDeath")) {
                        p.setGameMode(GameMode.SPECTATOR);
                    }
                    removePlayer(p.getName());
                    playersMessage(mm.deserialize(
                            "<gray>Спидраннер <gradient:#ff4444:#ffaaaa>" + p.getName() + "</gradient> погиб!"));
                    playersMessage(
                            mm.deserialize("<gray>Осталось спидраннеров: <white>" + speedrunners.size() + "</white>"));
                }
            }
        }
    }
}
