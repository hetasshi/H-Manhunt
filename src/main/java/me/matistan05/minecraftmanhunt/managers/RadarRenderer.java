package me.matistan05.minecraftmanhunt.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;

public class RadarRenderer {
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final int VISIBLE_CHARS = 25;

    private static final double DEGREES_PER_CHAR = 360.0 / 64.0;

    public Component render(Player viewer, List<Player> teammates) {
        float yaw = viewer.getLocation().getYaw();
        yaw = (yaw % 360 + 360) % 360;

        int centerIndex = (int) (yaw / DEGREES_PER_CHAR);

        String base = "S . . . SW . . . W . . . NW . . . N . . . NE . . . E . . . SE . . . ";
        String fullStrip = base + base + base;

        int offset = base.length();

        int startIndex = offset + centerIndex - (VISIBLE_CHARS / 2);

        net.kyori.adventure.text.TextComponent.Builder builder = net.kyori.adventure.text.Component.text();

        double startAngle = yaw - (VISIBLE_CHARS / 2.0 * DEGREES_PER_CHAR);

        for (int i = 0; i < VISIBLE_CHARS; i++) {
            double currentAngle = startAngle + (i * DEGREES_PER_CHAR);
            currentAngle = (currentAngle % 360 + 360) % 360;

            boolean hasTeammate = false;
            for (Player tm : teammates) {
                if (tm.equals(viewer))
                    continue;
                if (!tm.getWorld().equals(viewer.getWorld()))
                    continue;

                double angleToTarget = getAngleToTarget(viewer.getLocation(), tm.getLocation());
                double diff = Math.abs(angleToTarget - currentAngle);
                if (diff > 180)
                    diff = 360 - diff;

                if (diff < (DEGREES_PER_CHAR / 2.0)) {
                    hasTeammate = true;
                    break;
                }
            }

            char charAtPos = fullStrip.charAt(startIndex + i);

            if (hasTeammate) {
                builder.append(MM.deserialize("<green>♟</green>"));
            } else {
                if (charAtPos != ' ' && charAtPos != '.') {
                    builder.append(MM.deserialize("<white><bold>" + charAtPos + "</bold></white>"));
                } else {
                    builder.append(MM.deserialize("<gray>" + charAtPos + "</gray>"));
                }
            }
        }

        return builder.build();
    }

    private double getAngleToTarget(Location from, Location to) {
        Vector dir = to.toVector().subtract(from.toVector());
        double angle = Math.toDegrees(Math.atan2(-dir.getX(), dir.getZ()));
        return (angle % 360 + 360) % 360;
    }
}
