package me.matistan05.minecraftmanhunt.listeners;

import me.matistan05.minecraftmanhunt.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

import static me.matistan05.minecraftmanhunt.commands.ManhuntCommand.*;

public class AdvancementListener implements Listener {
    public AdvancementListener(Main main) {
        Bukkit.getPluginManager().registerEvents(this, main);
    }

    @EventHandler
    public void AdvancementEvent(PlayerAdvancementDoneEvent e) {
        if (!inGame)
            return;
        Player p = e.getPlayer();
        if ((e.getAdvancement().getKey().getKey().equals("end/kill_dragon")) && isSpeedrunner(p.getName())) {
            MiniMessage mm = MiniMessage.miniMessage();
            playersMessage(mm.deserialize(
                    "<gradient:#55ff55:#00aa00><bold>" + p.getName() + "</bold> убил дракона!</gradient>"));
            playersMessage(mm.deserialize("<gradient:#55ff55:#ffffff><bold>Спидраннеры победили!</bold></gradient>"));
            playersTitle(mm.deserialize("<gradient:#55ff55:#00aa00><bold>ПОБЕДА!</bold></gradient>"));
            reset();
        }
    }
}
