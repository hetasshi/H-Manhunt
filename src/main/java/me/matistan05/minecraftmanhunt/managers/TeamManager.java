package me.matistan05.minecraftmanhunt.managers;

import me.matistan05.minecraftmanhunt.Main;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class TeamManager {
    private final Main main;
    private final Scoreboard scoreboard;
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    public TeamManager(Main main) {
        this.main = main;
        this.scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
    }

    public void createTeams() {
        if (scoreboard.getTeam("hunters") != null) {
            scoreboard.getTeam("hunters").unregister();
        }
        Team huntersTeam = scoreboard.registerNewTeam("hunters");
        huntersTeam.setAllowFriendlyFire(main.getConfig().getBoolean("friendlyFire"));
        huntersTeam.color(NamedTextColor.RED);
        huntersTeam.prefix(LEGACY.deserialize("§7[§c§lHunter§7] §f"));
        huntersTeam.suffix(LEGACY.deserialize(" §c⚔"));
        huntersTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);

        if (scoreboard.getTeam("speedrunners") != null) {
            scoreboard.getTeam("speedrunners").unregister();
        }
        Team speedrunnersTeam = scoreboard.registerNewTeam("speedrunners");
        speedrunnersTeam.setAllowFriendlyFire(main.getConfig().getBoolean("friendlyFire"));
        speedrunnersTeam.color(NamedTextColor.GREEN);
        speedrunnersTeam.prefix(LEGACY.deserialize("§7[§a§lRunner§7] §f"));
        speedrunnersTeam.suffix(LEGACY.deserialize(" §a🗲"));
        speedrunnersTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
    }

    public void addHunter(String name) {
        Team team = scoreboard.getTeam("hunters");
        if (team != null) {
            team.addEntry(name);
        }
    }

    public void addSpeedrunner(String name) {
        Team team = scoreboard.getTeam("speedrunners");
        if (team != null) {
            team.addEntry(name);
        }
    }

    public void removePlayer(String name) {
        Team hunters = scoreboard.getTeam("hunters");
        if (hunters != null)
            hunters.removeEntry(name);

        Team speedrunners = scoreboard.getTeam("speedrunners");
        if (speedrunners != null)
            speedrunners.removeEntry(name);
    }

    public void reset() {
        Team hunters = scoreboard.getTeam("hunters");
        if (hunters != null)
            hunters.unregister();

        Team speedrunners = scoreboard.getTeam("speedrunners");
        if (speedrunners != null)
            speedrunners.unregister();
    }
}
