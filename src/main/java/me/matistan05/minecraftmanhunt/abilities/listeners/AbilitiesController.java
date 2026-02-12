package me.matistan05.minecraftmanhunt.abilities.listeners;

import me.matistan05.minecraftmanhunt.Main;
import me.matistan05.minecraftmanhunt.abilities.core.AbilitiesManager;
import me.matistan05.minecraftmanhunt.abilities.gui.AbilitiesMenu;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import static me.matistan05.minecraftmanhunt.commands.ManhuntCommand.*;

public class AbilitiesController implements Listener {
    private final Main main;
    private final AbilitiesManager abilitiesManager;
    private final AbilitiesMenu abilitiesMenu;

    public AbilitiesController(Main main, AbilitiesManager abilitiesManager, AbilitiesMenu abilitiesMenu) {
        this.main = main;
        this.abilitiesManager = abilitiesManager;
        this.abilitiesMenu = abilitiesMenu;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (!main.getConfig().getBoolean("casual") || !isHunter(p.getName()) || (!inGame && !waitingForStart))
            return;

        ItemStack item = e.getItem();
        if (item == null || item.getType() != Material.COMPASS || !waypointManager.isCompass(item))
            return;

        if (p.isSneaking() && (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            abilitiesMenu.open(p);
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player))
            return;
        Player p = (Player) e.getWhoClicked();

        MiniMessage mm = MiniMessage.miniMessage();
        if (e.getView().title().equals(mm.deserialize("<gradient:#ffaa00:#ffff55>Способности Охотника</gradient>"))) {
            e.setCancelled(true);
            ItemStack clickedItem = e.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR)
                return;

            if (clickedItem.getType() == Material.GOLD_INGOT) {
                abilitiesManager.useWarpShadows(p);
            }
        }
    }
}
