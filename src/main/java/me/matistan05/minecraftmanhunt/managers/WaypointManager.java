package me.matistan05.minecraftmanhunt.managers;

import me.matistan05.minecraftmanhunt.Main;
import me.matistan05.minecraftmanhunt.classes.Hunter;
import me.matistan05.minecraftmanhunt.classes.Speedrunner;
import me.matistan05.minecraftmanhunt.commands.ManhuntCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WaypointManager {
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private final Main main;
    private final NamespacedKey compassKey;
    private ItemStack compass;
    private final Map<UUID, Double> previousTransmitRange = new HashMap<>();
    private final Map<UUID, Double> previousReceiveRange = new HashMap<>();

    public WaypointManager(Main main) {
        this.main = main;
        this.compassKey = new NamespacedKey(main, "ManhuntCompass");
        createCompass();
    }

    private void createCompass() {
        ItemStack item = new ItemStack(Material.COMPASS, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(compassKey, PersistentDataType.BYTE, (byte) 1);
            meta.displayName(
                    MM.deserialize("<gradient:#ffb347:#ffcc33>Tracking:</gradient> <gray>nearest speedrunner</gray>"));
            List<Component> lore = new ArrayList<>();
            lore.add(MM.deserialize("<gray>Компас для отслеживания спидраннеров.</gray>"));
            meta.lore(lore);
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        compass = item;
    }

    public ItemStack getCompass() {
        return compass.clone();
    }

    public boolean isCompass(ItemStack itemStack) {
        if (itemStack == null)
            return false;
        if (!itemStack.hasItemMeta() || itemStack.getItemMeta() == null)
            return false;
        return (itemStack.getItemMeta().getPersistentDataContainer().has(compassKey, PersistentDataType.BYTE));
    }

    public int getCompassSlot(Player p) {
        if (p == null) {
            return -1;
        }
        for (int i = 0; i < 41; i++) {
            int slot = i;
            if (i == 36) {
                slot = 40;
            }
            if (isCompass(p.getInventory().getItem(slot))) {
                return slot;
            }
        }
        return -1;
    }

    public void updateCompasses(List<Hunter> hunters, List<Speedrunner> speedrunners) {
        for (Speedrunner speedrunnerObject : speedrunners) {
            Player speedrunner = Bukkit.getPlayerExact(speedrunnerObject.getName());
            if (speedrunner == null)
                continue;

            if (speedrunner.getWorld().getEnvironment().equals(World.Environment.NORMAL)) {
                speedrunnerObject.setLocWorld(speedrunner.getLocation());
            } else if (speedrunner.getWorld().getEnvironment().equals(World.Environment.NETHER)) {
                speedrunnerObject.setLocNether(speedrunner.getLocation());
            } else if (speedrunner.getWorld().getEnvironment().equals(World.Environment.THE_END)) {
                speedrunnerObject.setLocTheEnd(speedrunner.getLocation());
            }
        }

        for (Hunter hunterObject : hunters) {
            Player hunter = Bukkit.getPlayerExact(hunterObject.getName());
            if (hunter == null)
                continue;

            String targetName = hunterObject.getWhichSpeedrunner();
            Speedrunner targetSpeedrunner = ManhuntCommand.getSpeedrunner(targetName);
            Player target = (targetSpeedrunner != null) ? Bukkit.getPlayerExact(targetSpeedrunner.getName()) : null;

            Location targetLoc = null;
            String statusText = "";

            if (hunterObject.getCompassMode() == 0) {
                double minDistance = Double.MAX_VALUE;
                for (Speedrunner s : speedrunners) {
                    Player sp = Bukkit.getPlayerExact(s.getName());
                    Location potentialLoc = null;

                    if (sp != null && hunter.getWorld().equals(sp.getWorld())) {
                        potentialLoc = sp.getLocation();
                    }
                    else if (main.getConfig().getBoolean("trackPortals")) {
                        if (hunter.getWorld().getEnvironment() == World.Environment.NORMAL)
                            potentialLoc = s.getLocWorld();
                        else if (hunter.getWorld().getEnvironment() == World.Environment.NETHER)
                            potentialLoc = s.getLocNether();
                        else if (hunter.getWorld().getEnvironment() == World.Environment.THE_END)
                            potentialLoc = s.getLocTheEnd();
                    }

                    if (potentialLoc != null && hunter.getWorld().equals(potentialLoc.getWorld())) {
                        double dist = hunter.getLocation().distanceSquared(potentialLoc);
                        if (dist < minDistance) {
                            minDistance = dist;
                            targetLoc = potentialLoc;
                            statusText = "ближайший спидраннер"
                                    + (sp == null || !hunter.getWorld().equals(sp.getWorld()) ? " (портал)" : "");
                        }
                    }
                }
            } else if (targetSpeedrunner != null) {
                Location potentialLoc = null;
                if (target != null && hunter.getWorld().equals(target.getWorld())) {
                    potentialLoc = target.getLocation();
                } else if (main.getConfig().getBoolean("trackPortals")) {
                    if (hunter.getWorld().getEnvironment() == World.Environment.NORMAL)
                        potentialLoc = targetSpeedrunner.getLocWorld();
                    else if (hunter.getWorld().getEnvironment() == World.Environment.NETHER)
                        potentialLoc = targetSpeedrunner.getLocNether();
                    else if (hunter.getWorld().getEnvironment() == World.Environment.THE_END)
                        potentialLoc = targetSpeedrunner.getLocTheEnd();
                }

                if (potentialLoc != null && hunter.getWorld().equals(potentialLoc.getWorld())) {
                    targetLoc = potentialLoc;
                    statusText = targetSpeedrunner.getName()
                            + (target == null || !hunter.getWorld().equals(target.getWorld()) ? " (портал)" : "");
                }
            }

            int slot = getCompassSlot(hunter);
            if (slot != -1) {
                ItemStack hunterCompass = hunter.getInventory().getItem(slot);
                if (hunterCompass != null && hunterCompass.getType() == Material.COMPASS) {
                    if (hunter.getWorld().getEnvironment() == World.Environment.NORMAL) {
                        if (targetLoc != null) {
                            hunter.setCompassTarget(targetLoc);
                        }
                    }

                    boolean isNetherOrEnd = hunter.getWorld().getEnvironment() != World.Environment.NORMAL;
                    if (isNetherOrEnd || Bukkit.getCurrentTick() % 20 == 0) {
                        ItemMeta meta = hunterCompass.getItemMeta();
                        if (meta instanceof CompassMeta compassMeta) {
                            if (isNetherOrEnd) {
                                if (targetLoc != null) {
                                    compassMeta.setLodestone(targetLoc);
                                    compassMeta.setLodestoneTracked(false);
                                } else {
                                    compassMeta.setLodestone(null);
                                }
                            } else {
                                compassMeta.setLodestone(null);
                            }

                            if (Bukkit.getCurrentTick() % 20 == 0) {
                                if (targetLoc != null) {
                                    compassMeta.displayName(
                                            MM.deserialize("<gradient:#ffb347:#ffcc33>Цель:</gradient> <gray>"
                                                    + statusText + "</gray>"));
                                } else {
                                    String failReason = (targetSpeedrunner != null) ? "в другом мире" : "не найдена";
                                    compassMeta.displayName(MM.deserialize(
                                            "<gradient:#ff5555:#ff9999>Цель " + failReason + "</gradient>"));
                                }
                            }
                            hunterCompass.setItemMeta(compassMeta);
                        }
                    }
                }
            }
        }
    }

    public void setupWaypoints(Player player, String role) {
        boolean useBossBar = main.getConfig().getBoolean("useBossBarRadar", true);
        double transmitRange = 0;
        double receiveRange = 0;

        if (!useBossBar) {
            cacheWaypointAttributes(player);
            if (role.equalsIgnoreCase("hunter")) {
                transmitRange = 60000000;
                receiveRange = 60000000;
            } else {
                transmitRange = 0;
                receiveRange = 0;
            }
        }

        String attrTransmit = "minecraft:waypoint_transmit_range";
        String attrReceive = "minecraft:waypoint_receive_range";

        try {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    "attribute " + player.getName() + " " + attrTransmit + " base set " + transmitRange);

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    "attribute " + player.getName() + " " + attrReceive + " base set " + receiveRange);
        } catch (Exception e) {
        }
    }

    public void restoreWaypoints(Player player) {
        restoreAttribute(player, Attribute.WAYPOINT_TRANSMIT_RANGE, previousTransmitRange);
        restoreAttribute(player, Attribute.WAYPOINT_RECEIVE_RANGE, previousReceiveRange);
    }

    public void removeAllCompasses(Player player) {
        if (player == null)
            return;
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (isCompass(item)) {
                player.getInventory().clear(i);
            }
        }
        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (isCompass(offhand)) {
            player.getInventory().setItemInOffHand(null);
        }
    }

    private void cacheWaypointAttributes(Player player) {
        cacheAttribute(player, Attribute.WAYPOINT_TRANSMIT_RANGE, previousTransmitRange);
        cacheAttribute(player, Attribute.WAYPOINT_RECEIVE_RANGE, previousReceiveRange);
    }

    private void cacheAttribute(Player player, Attribute attribute, Map<UUID, Double> cache) {
        if (player == null || cache.containsKey(player.getUniqueId()))
            return;
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null) {
            cache.put(player.getUniqueId(), instance.getBaseValue());
        }
    }

    private void restoreAttribute(Player player, Attribute attribute, Map<UUID, Double> cache) {
        if (player == null)
            return;
        Double previous = cache.remove(player.getUniqueId());
        if (previous == null)
            return;
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null) {
            instance.setBaseValue(previous);
        }
    }
}
