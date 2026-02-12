package me.matistan05.minecraftmanhunt.managers;

import me.matistan05.minecraftmanhunt.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
        huntersTeam.setAllowFriendlyFire(main.getConfig().getBoolean("friendlyFire"));
        huntersTeam.setColor(ChatColor.RED);
        huntersTeam.setPrefix(ChatColor.GRAY + "[" + ChatColor.RED + ChatColor.BOLD + "Hunter" + ChatColor.GRAY
                + "] " + ChatColor.WHITE);
        huntersTeam.setSuffix(" " + ChatColor.RED + "⚔");
        huntersTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);

        if (scoreboard.getTeam("speedrunners") != null) {
            scoreboard.getTeam("speedrunners").unregister();
        }
        Team speedrunnersTeam = scoreboard.registerNewTeam("speedrunners");
        speedrunnersTeam.setAllowFriendlyFire(main.getConfig().getBoolean("friendlyFire"));
        speedrunnersTeam.setColor(ChatColor.GREEN);
        speedrunnersTeam.setPrefix(ChatColor.GRAY + "[" + ChatColor.GREEN + ChatColor.BOLD + "Runner"
                + ChatColor.GRAY + "] " + ChatColor.WHITE);
        speedrunnersTeam.setSuffix(" " + ChatColor.GREEN + "🗲");
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
