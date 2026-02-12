package me.matistan05.minecraftmanhunt.abilities.gui;

import me.matistan05.minecraftmanhunt.Main;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class AbilitiesMenu {
    private final Main main;

    public AbilitiesMenu(Main main) {
        this.main = main;
    }

    public void open(Player p) {
        MiniMessage mm = MiniMessage.miniMessage();
        Inventory inv = Bukkit.createInventory(p, 9,
                mm.deserialize("<gradient:#ffaa00:#ffff55>Способности Охотника</gradient>"));

        ItemStack warp = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta = warp.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize("<gradient:#aa00ff:#ff55ff><bold>Варп тени</bold></gradient>"));
            List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
            int dist = main.getConfig().getInt("warpShadowsMaxDistance");
            int cd = main.getConfig().getInt("warpShadowsCooldown");
            lore.add(mm.deserialize("<gray>Телепортация в сторону бегуна"));
            lore.add(mm.deserialize("<gray>на расстояние до <white>" + dist + "</white> блоков."));
            lore.add(net.kyori.adventure.text.Component.empty());
            lore.add(mm.deserialize("<gray>Перезарядка: <white>" + cd + "с</white>"));
            meta.lore(lore);
            warp.setItemMeta(meta);
        }

        inv.setItem(4, warp);
        p.openInventory(inv);
    }
}
