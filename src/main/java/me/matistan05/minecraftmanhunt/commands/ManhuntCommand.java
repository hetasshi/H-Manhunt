package me.matistan05.minecraftmanhunt.commands;

import me.matistan05.minecraftmanhunt.managers.RadarManager;
import me.matistan05.minecraftmanhunt.managers.TeamManager;
import me.matistan05.minecraftmanhunt.managers.WaypointManager;

import me.matistan05.minecraftmanhunt.Main;
import me.matistan05.minecraftmanhunt.classes.Hunter;
import me.matistan05.minecraftmanhunt.classes.Speedrunner;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import java.time.Duration;

import java.util.*;

@SuppressWarnings("deprecation")
public class ManhuntCommand implements CommandExecutor {
    private static Main main;
    public static List<Hunter> hunters = new ArrayList<>();
    public static List<Speedrunner> speedrunners = new ArrayList<>();
    public static int secondsToStart;
    public static boolean waitingForStart = false;
    public static boolean paused = false;
    public static boolean inGame = false;
    private static BukkitTask starting;
    public static BukkitTask game;
    public static BukkitTask pausing, unpausing;
    public static List<String> pausePlayers = new LinkedList<>();
    public static List<String> unpausePlayers = new LinkedList<>();
    public static Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
    public static Player findPlayer;
    public static TeamManager teamManager;
    public static WaypointManager waypointManager;
    public static RadarManager radarManager;

    public ManhuntCommand(Main main) {
        ManhuntCommand.main = main;
        teamManager = new TeamManager(main);
        waypointManager = new WaypointManager(main);
        radarManager = new RadarManager(main);
    }

    private static final MiniMessage mm = MiniMessage.miniMessage();

    @Override
    public boolean onCommand(CommandSender p, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            p.sendMessage(mm.deserialize(
                    "<gray>Вы должны ввести аргумент. Для помощи введите: <gradient:#ff4444:#ffaaaa>/manhunt help</gradient>"));
        } else if (args[0].equals("help")) {
            if (!p.hasPermission("manhunt.help") && main.getConfig().getBoolean("usePermissions")) {
                p.sendMessage(mm.deserialize("<red>У вас нет прав на использование этой команды."));
                return true;
            }
            if (args.length != 1) {
                p.sendMessage(mm.deserialize(
                        "<gray>Неправильное использование. Для помощи введите: <gradient:#ff4444:#ffaaaa>/manhunt help</gradient>"));
                return true;
            }
            p.sendMessage(mm.deserialize(
                    "<gradient:#ff0000:#ffffff><strikethrough>-------</strikethrough></gradient> <red>Minecraft Manhunt</red> <gradient:#ffffff:#ff0000><strikethrough>-------</strikethrough></gradient>"));
            p.sendMessage(mm.deserialize("<gray>Modded by <gradient:#ff0000:#aa0000>hetashi</gradient>"));
            p.sendMessage(mm.deserialize(""));
            p.sendMessage(mm.deserialize("<gradient:#ffcccc:#ffffff>Список команд:</gradient>"));
            p.sendMessage(mm.deserialize(
                    "<dark_gray>» <gradient:#ff4444:#ffaaaa>/manhunt add <роль> <игрок></gradient> <gray>- добавить игрока"));
            p.sendMessage(mm.deserialize(
                    "<dark_gray>» <gradient:#ff4444:#ffaaaa>/manhunt add <роль> @a</gradient> <gray>- добавить всех"));
            p.sendMessage(mm.deserialize(
                    "<dark_gray>» <gradient:#ff4444:#ffaaaa>/manhunt remove <игрок></gradient> <gray>- удалить игрока"));
            p.sendMessage(mm.deserialize(
                    "<dark_gray>» <gradient:#ff4444:#ffaaaa>/manhunt remove @a</gradient> <gray>- удалить всех"));
            p.sendMessage(mm.deserialize(
                    "<dark_gray>» <gradient:#ff0000:#ff5555><bold>/manhunt start</bold></gradient> <gray>- запуск игры"));
            p.sendMessage(mm.deserialize(
                    "<dark_gray>» <gradient:#aa0000:#ff0000><bold>/manhunt reset</bold></gradient> <gray>- сброс игры"));
            p.sendMessage(
                    mm.deserialize("<dark_gray>» <gradient:#ff8888:#ffcccc>/manhunt pause</gradient> <gray>- пауза"));
            p.sendMessage(mm.deserialize(
                    "<dark_gray>» <gradient:#ff8888:#ffcccc>/manhunt unpause</gradient> <gray>- продолжить"));
            p.sendMessage(mm.deserialize(
                    "<dark_gray>» <gradient:#ff8888:#ffcccc>/manhunt list</gradient> <gray>- список игроков"));
            p.sendMessage(
                    mm.deserialize("<dark_gray>» <gradient:#ff8888:#ffcccc>/manhunt rules</gradient> <gray>- правила"));
            p.sendMessage(mm.deserialize(
                    "<gradient:#ff0000:#ffffff><strikethrough>----------------------------------</strikethrough></gradient>"));
        } else if (args[0].equals("rules")) {
            if (!p.hasPermission("manhunt.rules") && main.getConfig().getBoolean("usePermissions")) {
                p.sendMessage(mm.deserialize("<red>У вас нет прав на использование этой команды."));
                return true;
            }
            if (args.length != 3 && args.length != 2) {
                p.sendMessage(mm.deserialize(
                        "<gray>Неправильное использование. Для помощи введите: <gradient:#ff4444:#ffaaaa>/manhunt help</gradient>"));
                return true;
            }
            if (!main.getConfig().contains(args[1])) {
                p.sendMessage(mm.deserialize("<red>Такого правила нет. Проверьте файл config.yml."));
                return true;
            }
            if (args.length == 2) {
                p.sendMessage(mm.deserialize("<gray>Значение правила <gradient:#ff4444:#ffaaaa>" + args[1]
                        + "</gradient> это: <white>" + main.getConfig().get(args[1])));
                return true;
            }
            if (args[1].equals("headStartDuration") || args[1].equals("speedrunnersLives")
                    || args[1].equals("warpShadowsCooldown") || args[1].equals("warpShadowsMaxDistance")
                    || args[1].equals("warpShadowsBufferZone")) {
                try {
                    main.getConfig().set(args[1], Integer.parseInt(args[2]));
                } catch (NumberFormatException e) {
                    p.sendMessage(mm.deserialize("<red>Значение должно быть числом!"));
                    return true;
                }
            } else {
                if (!args[2].equals("true") && !args[2].equals("false")) {
                    p.sendMessage(mm.deserialize("<red>Значение должно быть true или false!"));
                    return true;
                }
                main.getConfig().set(args[1], Boolean.parseBoolean(args[2]));
            }
            main.saveConfig();
            p.sendMessage(mm.deserialize("<gray>Значение правила <gradient:#ff4444:#ffaaaa>" + args[1]
                    + "</gradient> было изменено на: <white>" + args[2]));
        } else if (args[0].equals("add")) {
            if (!p.hasPermission("manhunt.add") && main.getConfig().getBoolean("usePermissions")) {
                p.sendMessage(mm.deserialize("<red>У вас нет прав на использование этой команды."));
                return true;
            }
            if (args.length < 3) {
                p.sendMessage(mm.deserialize(
                        "<gray>Неправильное использование. Для помощи введите: <gradient:#ff4444:#ffaaaa>/manhunt help</gradient>"));
                return true;
            }
            int count = 0;
            if (args[1].equals("speedrunner")) {
                if (args[2].equals("@a")) {
                    if (args.length != 3) {
                        p.sendMessage(mm.deserialize(
                                "<gray>Неправильное использование. Для помощи введите: <gradient:#ff4444:#ffaaaa>/manhunt help</gradient>"));
                        return true;
                    }
                    for (Player target : Bukkit.getOnlinePlayers()) {
                        if (isSpeedrunner(target.getName()))
                            continue;
                        if (isHunter(target.getName())) {
                            if (inGame || waitingForStart)
                                continue;
                            hunters.removeIf(h -> h.getName().equals(target.getName()));
                        }
                        speedrunners.add(new Speedrunner(target.getName()));
                        if (inGame || waitingForStart)
                            setUpPlayer(target.getName(), false);
                        count++;
                    }
                    if (count > 0) {
                        p.sendMessage(mm.deserialize("<gray>Успешно добавлено <gradient:#ff4444:#ffaaaa>" + count
                                + "</gradient> новых спидраннеров в игру!"));
                    } else {
                        p.sendMessage(mm.deserialize("<red>Спидраннеры не были добавлены!"));
                    }
                    return true;
                }
                for (int i = 2; i < args.length; i++) {
                    Player target = Bukkit.getPlayerExact(args[i]);
                    if (target == null || isSpeedrunner(target.getName()))
                        continue;
                    if (isHunter(target.getName())) {
                        if (inGame || waitingForStart)
                            continue;
                        hunters.removeIf(h -> h.getName().equals(target.getName()));
                    }
                    speedrunners.add(new Speedrunner(target.getName()));
                    if (inGame || waitingForStart)
                        setUpPlayer(target.getName(), false);
                    count++;
                }
                if (count > 0) {
                    p.sendMessage(mm.deserialize("<gray>Успешно добавлено <gradient:#ff4444:#ffaaaa>" + count
                            + "</gradient> новых спидраннеров в игру!"));
                } else {
                    p.sendMessage(mm.deserialize("<red>Не удалось добавить этих игроков!"));
                }
                return true;
            }
            if (args[1].equals("hunter")) {
                if (args[2].equals("@a")) {
                    if (args.length != 3) {
                        p.sendMessage(mm.deserialize(
                                "<gray>Неправильное использование. Для помощи введите: <gradient:#ff4444:#ffaaaa>/manhunt help</gradient>"));
                        return true;
                    }
                    for (Player target : Bukkit.getOnlinePlayers()) {
                        if (isHunter(target.getName()))
                            continue;
                        if (isSpeedrunner(target.getName())) {
                            if (inGame || waitingForStart)
                                continue;
                            speedrunners.removeIf(h -> h.getName().equals(target.getName()));
                        }
                        hunters.add(new Hunter(target.getName()));
                        if (inGame || waitingForStart)
                            setUpPlayer(target.getName(), true);
                        count++;
                    }
                    if (count > 0) {
                        p.sendMessage(mm.deserialize("<gray>Успешно добавлено <gradient:#ff4444:#ffaaaa>" + count
                                + "</gradient> новых хантеров в игру!"));
                    } else {
                        p.sendMessage(mm.deserialize("<red>Хантеры не были добавлены!"));
                    }
                    return true;
                }
                for (int i = 2; i < args.length; i++) {
                    Player target = Bukkit.getPlayerExact(args[i]);
                    if (target == null || isHunter(target.getName()))
                        continue;
                    if (isSpeedrunner(target.getName())) {
                        if (inGame || waitingForStart)
                            continue;
                        speedrunners.removeIf(h -> h.getName().equals(target.getName()));
                    }
                    hunters.add(new Hunter(target.getName()));
                    if (inGame || waitingForStart)
                        setUpPlayer(target.getName(), true);
                    count++;
                }
                if (count > 0) {
                    p.sendMessage(mm.deserialize("<gray>Успешно добавлено <gradient:#ff4444:#ffaaaa>" + count
                            + "</gradient> новых хантеров в игру!"));
                } else {
                    p.sendMessage(mm.deserialize("<red>Не удалось добавить этих игроков!"));
                }
                return true;
            }
            p.sendMessage(mm.deserialize("<red>Неверная роль! Помощь: /manhunt help"));
        } else if (args[0].equals("remove")) {
            if (!p.hasPermission("manhunt.remove") && main.getConfig().getBoolean("usePermissions")) {
                p.sendMessage(mm.deserialize("<red>У вас нет прав на использование этой команды."));
                return true;
            }
            if (args.length < 2) {
                p.sendMessage(mm.deserialize(
                        "<gray>Неправильное использование. Для помощи введите: <gradient:#ff4444:#ffaaaa>/manhunt help</gradient>"));
                return true;
            }
            int count = 0;
            if (args[1].equals("@a")) {
                if (args.length != 2) {
                    p.sendMessage(mm.deserialize(
                            "<gray>Неправильное использование. Для помощи введите: <gradient:#ff4444:#ffaaaa>/manhunt help</gradient>"));
                    return true;
                }
                for (Player target : Bukkit.getOnlinePlayers()) {
                    if (!isInGame(target.getName()))
                        continue;
                    removePlayer(target.getName());
                    count++;
                }
                if (count > 0) {
                    p.sendMessage(mm.deserialize("<gray>Успешно удалено <gradient:#ff4444:#ffaaaa>" + count
                            + "</gradient> игроков из игры!"));
                } else {
                    p.sendMessage(mm.deserialize("<red>Игроки не были удалены!"));
                }
                if ((inGame || waitingForStart) && (hunters.isEmpty() || speedrunners.isEmpty())) {
                    playersMessage(mm.deserialize(
                            "<gray>Игра остановлена: одна из ролей осталась без игроков.</gray>"));
                    reset();
                }
                return true;
            }
            for (int i = 1; i < args.length; i++) {
                Player target = Bukkit.getPlayerExact(args[i]);
                if (target == null || (!isInGame(target.getName())))
                    continue;
                removePlayer(target.getName());
                count++;
            }
            if (count > 0) {
                p.sendMessage(mm.deserialize(
                        "<gray>Успешно удалено <gradient:#ff4444:#ffaaaa>" + count + "</gradient> игроков из игры!"));
            } else {
                p.sendMessage(mm.deserialize("<red>Не удалось удалить этих игроков!"));
            }
            if ((inGame || waitingForStart) && (hunters.isEmpty() || speedrunners.isEmpty())) {
                playersMessage(mm.deserialize(
                        "<gray>Игра остановлена: одна из ролей осталась без игроков.</gray>"));
                reset();
            }
        } else if (args[0].equals("start")) {
            if (!p.hasPermission("manhunt.start") && main.getConfig().getBoolean("usePermissions")) {
                p.sendMessage(mm.deserialize("<red>У вас нет прав на использование этой команды."));
                return true;
            }
            if (args.length != 1) {
                p.sendMessage(mm.deserialize(
                        "<gray>Неправильное использование. Для помощи введите: <gradient:#ff4444:#ffaaaa>/manhunt help</gradient>"));
                return true;
            }
            if (speedrunners.size() + hunters.size() == 0) {
                p.sendMessage(mm.deserialize("<red>Недостаточно игроков (нет хантеров и спидраннеров)!"));
                return true;
            }
            if (speedrunners.isEmpty()) {
                p.sendMessage(mm.deserialize("<red>Нет спидраннеров!"));
                return true;
            }
            if (hunters.isEmpty()) {
                p.sendMessage(mm.deserialize("<red>Нет хантеров!"));
                return true;
            }
            if (inGame || waitingForStart) {
                p.sendMessage(mm.deserialize("<red>Игра уже идет!"));
                return true;
            }
            for (Speedrunner speedrunner : speedrunners) {
                Player player = Bukkit.getPlayerExact(speedrunner.getName());
                if (player == null) {
                    p.sendMessage(mm.deserialize("<red>Кто-то из игроков оффлайн!"));
                    return true;
                }
            }
            for (Hunter hunter : hunters) {
                Player player = Bukkit.getPlayerExact(hunter.getName());
                if (player == null) {
                    p.sendMessage(mm.deserialize("<red>Кто-то из игроков оффлайн!"));
                    return true;
                }
            }
            if (main.getConfig().getBoolean("timeSetDayOnStart")) {
                setTimeAllWorlds(0);
            }
            if (main.getConfig().getBoolean("weatherClearOnStart")) {
                setWeatherAllWorlds(false);
            }
            findPlayer = null;
            if (main.getConfig().getBoolean("useBossBarRadar", true)) {
                radarManager.start();
            } else {
                setLocatorBarAllWorlds(true);
            }

            if (main.getConfig().getBoolean("teleport")) {
                for (Speedrunner speedrunnerObject : speedrunners) {
                    Player pl = Bukkit.getPlayerExact(speedrunnerObject.getName());
                    if (pl != null) {
                        findPlayer = pl;
                        break;
                    }
                }
                if (findPlayer == null) {
                    for (Hunter hunter : hunters) {
                        Player pl = Bukkit.getPlayerExact(hunter.getName());
                        if (pl != null) {
                            findPlayer = pl;
                            break;
                        }
                    }
                }
            }

            teamManager.createTeams();

            for (Hunter hunterObject : hunters) {
                setUpPlayer(hunterObject.getName(), true);
            }
            for (Speedrunner speedrunnerObject : speedrunners) {
                setUpPlayer(speedrunnerObject.getName(), false);
            }
            waitingForStart = true;
            secondsToStart = Math.max(main.getConfig().getInt("headStartDuration"), 0);
            starting = new BukkitRunnable() {
                @Override
                public void run() {
                    if (secondsToStart == 0) {
                        inGame = true;
                        waitingForStart = false;
                        start();
                        starting.cancel();
                    } else {
                        playersMessage(mm.deserialize(
                                "<gray>Осталось <gradient:#ff4444:#ffaaaa>" + secondsToStart + "</gradient> секунд!"));
                        playersTitle(mm.deserialize("<dark_purple>" + secondsToStart));
                        secondsToStart -= 1;
                    }
                }
            }.runTaskTimer(main, 0, 20);
            game = new BukkitRunnable() {
                @Override
                public void run() {
                    waypointManager.updateCompasses(hunters, speedrunners);
                }
            }.runTaskTimer(main, 0, 1);

        } else if (args[0].equals("reset")) {
            if (!p.hasPermission("manhunt.reset") && main.getConfig().getBoolean("usePermissions")) {
                p.sendMessage(mm.deserialize("<red>У вас нет прав на использование этой команды."));
                return true;
            }
            if (args.length != 1) {
                p.sendMessage(mm.deserialize(
                        "<gray>Неправильное использование. Для помощи введите: <gradient:#ff4444:#ffaaaa>/manhunt help</gradient>"));
                return true;
            }
            p.sendMessage(mm.deserialize("<gray>Игра <gradient:#aa0000:#ff0000>сброшена</gradient>!"));
            reset();
        } else if (args[0].equals("pause")) {
            if (args.length != 1) {
                p.sendMessage(mm.deserialize(
                        "<gray>Неправильное использование. Для помощи введите: <gradient:#ff4444:#ffaaaa>/manhunt help</gradient>"));
                return true;
            }
            if (!p.hasPermission("manhunt.pause") && main.getConfig().getBoolean("usePermissions")
                    && !isInGame(p.getName())) {
                p.sendMessage(mm.deserialize("<red>Вы должны быть в игре, чтобы голосовать, или иметь права!"));
                return true;
            }
            if (!inGame) {
                p.sendMessage(mm.deserialize("<red>Игра не запущена!"));
                return true;
            }
            if (!main.getConfig().getBoolean("enablePauses")) {
                p.sendMessage(mm.deserialize("<red>Паузы отключены!"));
                return true;
            }
            if (paused) {
                p.sendMessage(mm.deserialize("<red>Игра уже на паузе!"));
                return true;
            }
            if (pausePlayers.contains(p.getName())) {
                p.sendMessage(mm.deserialize("<red>Вы уже проголосовали за паузу!"));
                return true;
            }
            if (p.hasPermission("manhunt.pause")) {
                pauseGame(p);
                return true;
            }
            pausePlayers.add(p.getName());
            playersMessage(mm.deserialize("<gray>Игрок <gradient:#ff4444:#ffaaaa>" + p.getName()
                    + "</gradient> хочет поставить паузу! (<white>" + pausePlayers.size() + "/"
                    + (hunters.size() + speedrunners.size()) + "</white>)"));
            if (pausePlayers.size() == hunters.size() + speedrunners.size()) {
                pauseGame(p);
                return true;
            }
            if (pausePlayers.size() == 1) {
                pausing = new BukkitRunnable() {
                    @Override
                    public void run() {
                        pausePlayers.clear();
                        playersMessage(mm.deserialize("<gray>Голосование за паузу истекло"));
                    }
                }.runTaskLater(main, 1200);
            }
        } else if (args[0].equals("unpause")) {
            if (args.length != 1) {
                p.sendMessage(mm.deserialize(
                        "<gray>Неправильное использование. Для помощи введите: <gradient:#ff4444:#ffaaaa>/manhunt help</gradient>"));
                return true;
            }
            if (!p.hasPermission("manhunt.unpause") && main.getConfig().getBoolean("usePermissions")
                    && !isInGame(p.getName())) {
                p.sendMessage(mm.deserialize("<red>Вы должны быть в игре, чтобы голосовать, или иметь права!"));
                return true;
            }
            if (!inGame) {
                p.sendMessage(mm.deserialize("<red>Игра не запущена!"));
                return true;
            }
            if (!main.getConfig().getBoolean("enablePauses")) {
                p.sendMessage(mm.deserialize("<red>Паузы отключены!"));
                return true;
            }
            if (!paused) {
                p.sendMessage(mm.deserialize("<red>Игра не на паузе!"));
                return true;
            }
            if (unpausePlayers.contains(p.getName())) {
                p.sendMessage(mm.deserialize("<red>Вы уже проголосовали за продолжение!"));
                return true;
            }
            if (p.hasPermission("manhunt.unpause")) {
                unpauseGame(p);
                return true;
            }
            unpausePlayers.add(p.getName());
            playersMessage(mm.deserialize("<gray>Игрок <gradient:#ff4444:#ffaaaa>" + p.getName()
                    + "</gradient> хочет продолжить игру! (<white>" + unpausePlayers.size() + "/"
                    + (hunters.size() + speedrunners.size()) + "</white>)"));
            if (unpausePlayers.size() == hunters.size() + speedrunners.size()) {
                unpauseGame(p);
                return true;
            }
            if (unpausePlayers.size() == 1) {
                unpausing = new BukkitRunnable() {
                    @Override
                    public void run() {
                        unpausePlayers.clear();
                        playersMessage(mm.deserialize("<gray>Голосование за продолжение истекло"));
                    }
                }.runTaskLater(main, 1200);
            }
        } else if (args[0].equals("list")) {
            if (!p.hasPermission("manhunt.list") && main.getConfig().getBoolean("usePermissions")) {
                p.sendMessage(mm.deserialize("<red>У вас нет прав на использование этой команды."));
                return true;
            }
            if (args.length != 1) {
                p.sendMessage(mm.deserialize(
                        "<gray>Неправильное использование. Для помощи введите: <gradient:#ff4444:#ffaaaa>/manhunt help</gradient>"));
                return true;
            }
            if (speedrunners.size() + hunters.size() == 0) {
                p.sendMessage(mm.deserialize("<red>Нет игроков в вашей игре!"));
                return true;
            }
            p.sendMessage(mm.deserialize(
                    "<gradient:#ff0000:#ffffff><strikethrough>-------</strikethrough></gradient> <red>Minecraft Manhunt</red> <gradient:#ffffff:#ff0000><strikethrough>-------</strikethrough></gradient>"));
            if (!speedrunners.isEmpty()) {
                p.sendMessage(mm.deserialize("<gradient:#ff4444:#ffaaaa>Спидраннеры:</gradient>"));
                for (Speedrunner speedrunnerObject : speedrunners) {
                    p.sendMessage(mm.deserialize("<dark_green>" + speedrunnerObject.getName()));
                }
            }
            if (!hunters.isEmpty()) {
                p.sendMessage(mm.deserialize("<gradient:#aa0000:#ff0000>Хантеры:</gradient>"));
                for (Hunter hunterObject : hunters) {
                    p.sendMessage(mm.deserialize("<dark_red>" + hunterObject.getName()));
                }
            }
            p.sendMessage(mm.deserialize(
                    "<gradient:#ff0000:#ffffff><strikethrough>----------------------------------</strikethrough></gradient>"));
        } else {
            p.sendMessage(mm.deserialize("<red>Неверный аргумент. Для помощи введите: /manhunt help"));
        }
        return true;
    }

    public static void removePlayer(String name) {
        Hunter hunterObject = getHunter(name);
        Speedrunner speedrunnerObject = getSpeedrunner(name);

        if (hunterObject != null) {
            Player hunter = Bukkit.getPlayerExact(name);
            if (inGame || waitingForStart) {
                if (main.getConfig().getBoolean("takeAwayOps")) {
                    OfflinePlayer target = Bukkit.getOfflinePlayer(name);
                    target.setOp(hunterObject.isOp());
                }
                if (hunter != null) {
                    waypointManager.removeAllCompasses(hunter);
                    waypointManager.restoreWaypoints(hunter);
                }
            }
            hunters.removeIf(h -> h.getName().equals(name));
        } else if (speedrunnerObject != null) {
            if (inGame || waitingForStart) {
                if (main.getConfig().getBoolean("takeAwayOps")) {
                    OfflinePlayer target = Bukkit.getOfflinePlayer(name);
                    target.setOp(speedrunnerObject.isOp());
                }
                Player speedrunner = Bukkit.getPlayerExact(name);
                if (speedrunner != null) {
                    waypointManager.restoreWaypoints(speedrunner);
                }
            }
            speedrunners.removeIf(s -> s.getName().equals(name));
        }
        teamManager.removePlayer(name);
    }

    public static void reset() {
        while (!hunters.isEmpty()) {
            removePlayer(hunters.get(0).getName());
        }
        while (!speedrunners.isEmpty()) {
            removePlayer(speedrunners.get(0).getName());
        }
        teamManager.reset();
        if (game != null && !game.isCancelled()) {
            game.cancel();
        }
        inGame = false;
        if (radarManager != null) {
            radarManager.stop();
        }
        setLocatorBarAllWorlds(false);
        if (waitingForStart) {
            if (starting != null && !starting.isCancelled()) {
                starting.cancel();
            }
        }
        waitingForStart = false;
        if (pausing != null && !pausing.isCancelled()) {
            pausing.cancel();
        }
        if (unpausing != null && !unpausing.isCancelled()) {
            unpausing.cancel();
        }
        pausePlayers.clear();
        unpausePlayers.clear();
        paused = false;
    }

    public static void playersMessage(String s) {
        playersMessage(Component.text(s));
    }

    public static void playersTitle(String s) {
        playersTitle(Component.text(s));
    }

    public static void playersMessage(Component s) {
        for (Hunter hunterObject : hunters) {
            Player hunter = Bukkit.getPlayerExact(hunterObject.getName());
            if (hunter != null) {
                hunter.sendMessage(s);
            }
        }
        for (Speedrunner speedrunnerObject : speedrunners) {
            Player speedrunner = Bukkit.getPlayerExact(speedrunnerObject.getName());
            if (speedrunner != null) {
                speedrunner.sendMessage(s);
            }
        }
    }

    public static void playersTitle(Component s) {
        Title title = Title.title(s, Component.empty(),
                Title.Times.times(Duration.ZERO, Duration.ofMillis(1000), Duration.ofMillis(500)));
        for (Hunter hunterObject : hunters) {
            Player hunter = Bukkit.getPlayerExact(hunterObject.getName());
            if (hunter != null) {
                hunter.showTitle(title);
            }
        }
        for (Speedrunner speedrunnerObject : speedrunners) {
            Player speedrunner = Bukkit.getPlayerExact(speedrunnerObject.getName());
            if (speedrunner != null) {
                speedrunner.showTitle(title);
            }
        }
    }

    public static void start() {
        playersTitle(mm.deserialize("<gradient:#aa0000:#ff0000><bold>СТАРТ!</bold></gradient>"));
        playersMessage(mm.deserialize("<gradient:#ff4444:#ffaaaa>Игра началась!</gradient>"));
        for (Hunter hunterObject : hunters) {
            Player hunter = Bukkit.getPlayerExact(hunterObject.getName());
            if (hunter != null)
                hunter.setFallDistance(0);
        }
    }

    public static void setUpPlayer(String name, boolean isHunter) {
        Player player = Bukkit.getPlayerExact(name);
        if (player == null)
            return;
        if (Bukkit.getScoreboardManager() != null) {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
        if (main.getConfig().getBoolean("clearInventories")) {
            player.getInventory().clear();
        }
        if (main.getConfig().getBoolean("teleport") && !inGame) {
            player.teleport(findPlayer);
        }
        player.setGameMode(GameMode.SURVIVAL);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setSaturation(5);
        Iterator<Advancement> advancements = Bukkit.getServer().advancementIterator();
        while (advancements.hasNext()) {
            AdvancementProgress progress = player.getAdvancementProgress(advancements.next());
            for (String s : progress.getAwardedCriteria())
                progress.revokeCriteria(s);
        }
        if (isHunter) {
            teamManager.addHunter(name);
            Hunter hunterObject = getHunter(name);
            waypointManager.removeAllCompasses(player);
            player.getInventory().setItem(8, waypointManager.getCompass());
            hunterObject.setWhichSpeedrunner(speedrunners.get(0).getName());
            hunterObject.setCompassMode(1);
            if (main.getConfig().getBoolean("takeAwayOps")) {
                hunterObject.setOp(player.isOp());
                player.setOp(false);
            }
            waypointManager.setupWaypoints(player, "hunter");
        } else {
            teamManager.addSpeedrunner(name);
            Speedrunner speedrunnerObject = getSpeedrunner(name);
            if (player.getWorld().getEnvironment().equals(World.Environment.NORMAL)) {
                speedrunnerObject.setLocWorld(player.getLocation());
            } else
                speedrunnerObject.setLocWorld(null);
            if (player.getWorld().getEnvironment().equals(World.Environment.NETHER)) {
                speedrunnerObject.setLocNether(player.getLocation());
            } else
                speedrunnerObject.setLocNether(null);
            if (player.getWorld().getEnvironment().equals(World.Environment.THE_END)) {
                speedrunnerObject.setLocTheEnd(player.getLocation());
            } else
                speedrunnerObject.setLocTheEnd(null);
            speedrunnerObject.setLives(Math.max(main.getConfig().getInt("speedrunnersLives"), 1));
            if (main.getConfig().getBoolean("spectatorAfterDeath")) {
                speedrunnerObject.setGameMode(player.getGameMode());
            }
            if (main.getConfig().getBoolean("takeAwayOps")) {
                speedrunnerObject.setOp(player.isOp());
                player.setOp(false);
            }
            waypointManager.setupWaypoints(player, "speedrunner");
        }
    }

    public static void unpauseGame(CommandSender p) {
        paused = false;
        if (unpausing != null && !unpausing.isCancelled())
            unpausing.cancel();
        pausePlayers.clear();
        playersMessage(mm.deserialize("<gray>Игра <gradient:#ff4444:#ffaaaa>возобновлена</gradient>!"));
        setDaylightCycleAllWorlds(true);
        for (Hunter hunterObject : hunters) {
            Player hunter = Bukkit.getPlayerExact(hunterObject.getName());
            if (hunter != null) {
                hunter.setFallDistance(0);
                hunter.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 255));
            }
        }
        for (Speedrunner speedrunnerObject : speedrunners) {
            Player speedrunner = Bukkit.getPlayerExact(speedrunnerObject.getName());
            if (speedrunner != null) {
                speedrunner.setFallDistance(0);
                speedrunner.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 255));
            }
        }
    }

    public static void pauseGame(CommandSender p) {
        paused = true;
        if (pausing != null && !pausing.isCancelled())
            pausing.cancel();
        unpausePlayers.clear();
        playersMessage(mm.deserialize("<gray>Игра <gradient:#aa0000:#ff0000>приостановлена</gradient>!"));
        setDaylightCycleAllWorlds(false);
    }

    private static void setDaylightCycleAllWorlds(boolean enabled) {
        for (World world : Bukkit.getWorlds()) {
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, enabled);
        }
    }

    private static void setTimeAllWorlds(long time) {
        for (World world : Bukkit.getWorlds()) {
            world.setTime(time);
        }
    }

    private static void setWeatherAllWorlds(boolean storm) {
        for (World world : Bukkit.getWorlds()) {
            world.setStorm(storm);
        }
    }

    private static void setLocatorBarAllWorlds(boolean enabled) {
        for (World world : Bukkit.getWorlds()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    "execute in " + world.getName() + " run gamerule locator_bar " + enabled);
        }
    }

    public static boolean isHunter(String name) {
        return hunters.stream().anyMatch(h -> h.getName().equals(name));
    }

    public static boolean isSpeedrunner(String name) {
        return speedrunners.stream().anyMatch(s -> s.getName().equals(name));
    }

    public static boolean isInGame(String name) {
        return isHunter(name) || isSpeedrunner(name);
    }

    public static Speedrunner getSpeedrunner(String name) {
        return speedrunners.stream().filter(s -> s.getName().equals(name)).findFirst().orElse(null);
    }

    public static Hunter getHunter(String name) {
        return hunters.stream().filter(h -> h.getName().equals(name)).findFirst().orElse(null);
    }
}
