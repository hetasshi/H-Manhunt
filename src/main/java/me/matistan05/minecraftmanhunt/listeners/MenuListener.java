package me.matistan05.minecraftmanhunt.listeners;

import me.matistan05.minecraftmanhunt.Main;
import org.bukkit.Bukkit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.meta.SkullMeta;

import static me.matistan05.minecraftmanhunt.commands.ManhuntCommand.*;

public class MenuListener implements Listener {
    public MenuListener(Main main) {
        Bukkit.getPluginManager().registerEvents(this, main);
    }

    @EventHandler
    public void MenuClickEvent(InventoryClickEvent e) {
        if (!inGame && !waitingForStart)
            return;
        Component title = e.getView().title();

        if (title.equals(MiniMessage.miniMessage().deserialize("<red>Выберите спидраннера!</red>"))) {
            handleChooseSpeedrunner(e);
        }
    }

    private void handleChooseSpeedrunner(InventoryClickEvent e) {
        if (e.getCurrentItem() == null)
            return;
        if (e.getCurrentItem().getType() == Material.PLAYER_HEAD) {
            SkullMeta headMeta = (SkullMeta) e.getCurrentItem().getItemMeta();
            getHunter(e.getWhoClicked().getName()).setWhichSpeedrunner(headMeta.getOwningPlayer().getName());
            if (getHunter(e.getWhoClicked().getName()).getCompassMode() == 0) {
                getHunter(e.getWhoClicked().getName()).setCompassMode(1);
            }
        }
        e.setCancelled(true);
        if (e.getCurrentItem().getType() == Material.PLAYER_HEAD) {
            e.getWhoClicked().closeInventory();
        }
    }
}
