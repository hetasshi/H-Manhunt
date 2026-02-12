package me.matistan05.minecraftmanhunt.managers;

import me.matistan05.minecraftmanhunt.Main;
import me.matistan05.minecraftmanhunt.commands.ManhuntCommand;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class RadarManager {
    private final Main main;
    private final RadarRenderer renderer;
    private final Map<UUID, BossBar> bars = new HashMap<>();
    private BukkitTask task;

    public RadarManager(Main main) {
        this.main = main;
        this.renderer = new RadarRenderer();
    }

    public void start() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }

        task = new BukkitRunnable() {
            @Override
            public void run() {
                update();
            }
        }.runTaskTimer(main, 0L, 2L);
    }

    public void stop() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
        removeAllBars();
    }

    private void update() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!ManhuntCommand.isInGame(p.getName())) {
                removeBar(p);
                continue;
            }

            BossBar bar = getOrCreateBar(p);
            List<Player> teammates = new ArrayList<>();
            String roleColor = "white";

            if (ManhuntCommand.isHunter(p.getName())) {
                roleColor = "green";
                for (var h : ManhuntCommand.hunters) {
                    Player hp = Bukkit.getPlayerExact(h.getName());
                    if (hp != null)
                        teammates.add(hp);
                }
            } else if (ManhuntCommand.isSpeedrunner(p.getName())) {
                roleColor = "red";
                if (ManhuntCommand.speedrunners.size() > 1) {
                    for (var s : ManhuntCommand.speedrunners) {
                        Player sp = Bukkit.getPlayerExact(s.getName());
                        if (sp != null)
                            teammates.add(sp);
                    }
                }
            }

            Component title = renderer.render(p, teammates);
            bar.name(title);
            bar.progress(1.0f);

            p.showBossBar(bar);
        }
    }

    private BossBar getOrCreateBar(Player p) {
        return bars.computeIfAbsent(p.getUniqueId(), k -> {
            BossBar bar = BossBar.bossBar(
                    Component.empty(),
                    1.0f,
                    BossBar.Color.WHITE,
                    BossBar.Overlay.PROGRESS);
            return bar;
        });
    }

    public void removeBar(Player p) {
        BossBar bar = bars.remove(p.getUniqueId());
        if (bar != null) {
            p.hideBossBar(bar);
        }
    }

    public void removeAllBars() {
        for (UUID uuid : bars.keySet()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.hideBossBar(bars.get(uuid));
            }
        }
        bars.clear();
    }
}
