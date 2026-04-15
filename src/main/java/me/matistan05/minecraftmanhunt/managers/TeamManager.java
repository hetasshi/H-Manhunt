package me.matistan05.minecraftmanhunt.managers;

import me.matistan05.minecraftmanhunt.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class TeamManager {
    private final Main main;
    private final Scoreboard scoreboard;

    public TeamManager(Main main) {
        this.main = main;
        this.scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
    }

    public void createTeams() {
        if (scoreboard.getTeam("hunters") != null) {
            scoreboard.getTeam("hunters").unregister();
        }
        Team huntersTeam = scoreboard.registerNewTeam("hunters");
        huntersTeam.setAllowFriendlyFire(main.getConfig().getBoolean("gameplay.friendly-fire"));
        huntersTeam.color(NamedTextColor.WHITE);
        huntersTeam.prefix(Component.empty());
        huntersTeam.suffix(Component.empty());
        huntersTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);

        if (scoreboard.getTeam("speedrunners") != null) {
            scoreboard.getTeam("speedrunners").unregister();
        }
        Team speedrunnersTeam = scoreboard.registerNewTeam("speedrunners");
        speedrunnersTeam.setAllowFriendlyFire(main.getConfig().getBoolean("gameplay.friendly-fire"));
        speedrunnersTeam.color(NamedTextColor.WHITE);
        speedrunnersTeam.prefix(Component.empty());
        speedrunnersTeam.suffix(Component.empty());
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
