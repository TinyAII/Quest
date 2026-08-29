package nl.tinyaii.quest.command;

import nl.tinyaii.quest.QuestPlugin;
import nl.tinyaii.quest.gui.QuestMenu;
import nl.tinyaii.quest.quest.Quest;
import nl.tinyaii.quest.quest.QuestService;
import nl.tinyaii.quest.util.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QuestCommand implements CommandExecutor, TabCompleter {
    private final QuestPlugin plugin;
    private final QuestService service;

    public QuestCommand(QuestPlugin plugin) {
        this.plugin = plugin;
        this.service = new QuestService(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // 英文子命令归一化（info/claim/auto... → 中文）
        for (int i = 0; i < args.length; i++) args[i] = normalize(args[i]);
        Messages msg = plugin.getMessages();

        // /任务 → 打开面板
        if (args.length == 0) {
            if (!(sender instanceof Player)) { sender.sendMessage("控制台请用: /任务 进度 <玩家>"); return true; }
            if (!sender.hasPermission("quest.use")) { msg.send((Player) sender, "no-permission"); return true; }
            new QuestMenu(plugin, (Player) sender).open();
            return true;
        }

        switch (args[0]) {
            case "领取": {
                if (!(sender instanceof Player)) { sender.sendMessage("仅玩家可用。"); return true; }
                if (!checkUse((Player) sender)) return true;
                if (args.length < 2) { sender.sendMessage(Messages.color("&c用法: /任务 领取 <任务id>")); return true; }
                String err = service.startQuest((Player) sender, args[1]);
                if (err != null) {
                    switch (err) {
                        case "quest-not-found" -> msg.send((Player)sender, "quest-not-found", "{quest}", args[1]);
                        case "quest-already-active" -> msg.send((Player)sender, "quest-already-active");
                        case "quest-chain-locked" -> msg.send((Player)sender, "quest-chain-locked");
                        case "daily-not-available" -> sender.sendMessage(Messages.color("&c今日任务尚未刷新，请等待或联系管理员。"));
                        case "quest-already-completed" -> sender.sendMessage(Messages.color("&c该任务今日已完成。"));
                    }
                } else {
                    Quest q = plugin.getQuestManager().get(args[1]);
                    msg.send((Player)sender, "quest-started", "{quest}", q == null ? args[1] : q.getName());
                }
                return true;
            }
            case "进度": {
                if (!(sender instanceof Player)) { sender.sendMessage("控制台请用: /任务 进度 <玩家>"); return true; }
                if (!checkUse((Player) sender)) return true;
                Player p = (Player) sender;
                var prog = plugin.getProgressManager().get(p.getUniqueId());
                if (prog.active.isEmpty()) { msg.send(p, "no-active-quests"); return true; }
                for (String id : prog.active.keySet()) {
                    Quest q = plugin.getQuestManager().get(id);
                    if (q == null) continue;
                    p.sendMessage(msg.raw("quest-progress",
                            "{quest}", q.getName(),
                            "{current}", String.valueOf(prog.active.get(id)),
                            "{target}", String.valueOf(q.getAmount())));
                }
                return true;
            }
            case "自动领取": {
                if (!(sender instanceof Player)) { sender.sendMessage("仅玩家可用。"); return true; }
                Player ap = (Player) sender;
                if (!checkUse(ap)) return true;
                boolean on = true;
                if (args.length >= 2) {
                    String v = args[1];
                    on = v.equals("开") || v.equals("on") || v.equals("true") || v.equals("1");
                }
                var prog = plugin.getProgressManager().get(ap.getUniqueId());
                prog.autoPickDaily = on;
                plugin.getProgressManager().save();
                if (on) {
                    // 开启后立即抽+领
                    plugin.getQuestService().pickDailyIfNeeded(ap);
                    int got = 0;
                    for (String id : new java.util.ArrayList<>(prog.dailyPicked)) {
                        if (prog.active.containsKey(id) || prog.completedToday.contains(id)) continue;
                        String err = plugin.getQuestService().startQuest(ap, id);
                        if (err == null) got++;
                    }
                    ap.sendMessage(Messages.color("&a已开启自动领取今日任务" + (got > 0 ? "，本次领取 &e" + got + " &a个。" : "。")));
                } else {
                    ap.sendMessage(Messages.color("&c已关闭自动领取今日任务。"));
                }
                return true;
            }
            case "完成": {
                if (!(sender instanceof Player)) { sender.sendMessage("仅玩家可用。"); return true; }
                if (!checkUse((Player) sender)) return true;
                if (args.length < 2) { sender.sendMessage(Messages.color("&c用法: /任务 完成 <任务id>")); return true; }
                if (!service.claim((Player) sender, args[1])) {
                    // claim false：可能不在进行中或未完成
                    var prog = plugin.getProgressManager().get(((Player)sender).getUniqueId());
                    if (!prog.active.containsKey(args[1])) {
                        msg.send((Player)sender, "quest-not-active", "{quest}", args[1]);
                    }
                }
                return true;
            }

            // ---- 管理 ----
            case "刷新": {
                if (!checkAdmin(sender)) return true;
                int n = plugin.refreshDaily();
                if (sender instanceof Player) msg.send((Player) sender, "daily-refreshed", "{count}", String.valueOf(n));
                else sender.sendMessage(msg.raw("daily-refreshed", "{count}", String.valueOf(n)));
                return true;
            }
            case "重置": {
                if (!checkAdmin(sender)) return true;
                if (args.length < 2) { sender.sendMessage(Messages.color("&c用法: /任务 重置 <玩家>")); return true; }
                org.bukkit.OfflinePlayer t = org.bukkit.Bukkit.getOfflinePlayer(args[1]);
                plugin.getProgressManager().reset(t.getUniqueId());
                if (sender instanceof Player) msg.send((Player) sender, "reset-player", "{player}", args[1]);
                else sender.sendMessage(msg.raw("reset-player", "{player}", args[1]));
                return true;
            }
            case "重载": {
                if (!checkAdmin(sender)) return true;
                plugin.reloadAll();
                if (sender instanceof Player) msg.send((Player) sender, "reloaded");
                else sender.sendMessage(msg.raw("reloaded"));
                return true;
            }
            default:
                sendHelp(sender);
                return true;
        }
    }

    private void sendHelp(CommandSender s) {
        String[] lines = {
                "&6===== Quest 任务系统 =====",
                "&e/任务 &7- 打开任务面板（中英文子命令均可）",
                "&e/任务 领取 <id> &7- 领取任务",
                "&e/任务 进度 &7- 当前任务进度",
                "&e/任务 完成 <id> &7- 手动提交（自动完成模式不需要）",
                "&e/任务 自动领取 [开|关] &7- 进服自动领今日任务（玩家自用）",
                "&c--- 管理 ---",
                "&e/任务 刷新 &7- 刷新每日任务板",
                "&e/任务 重置 <玩家>",
                "&e/任务 重载"
        };
        for (String l : lines) s.sendMessage(Messages.color(l));
    }

    /** 英文→中文子命令归一化 */
    private String normalize(String input) {
        if (input == null) return input;
        switch (input.toLowerCase()) {
            case "info": return "信息";
            case "claim": return "领取";
            case "progress": return "进度";
            case "complete": return "完成";
            case "auto": case "autopick": case "dailyauto": return "自动领取";
            case "refresh": return "刷新";
            case "reset": return "重置";
            case "reload": return "重载";
            default: return input;
        }
    }

    private boolean checkUse(Player p) {
        if (p.hasPermission("quest.use")) return true;
        plugin.getMessages().send(p, "no-permission");
        return false;
    }

    private boolean checkAdmin(CommandSender s) {
        if (s.hasPermission("quest.admin")) return true;
        if (s instanceof Player) plugin.getMessages().send((Player) s, "no-permission");
        else s.sendMessage(plugin.getMessages().raw("no-permission"));
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            List<String> subs = new ArrayList<>(Arrays.asList("领取", "进度", "完成", "自动领取", "claim", "info", "auto"));
            if (sender.hasPermission("quest.admin")) subs.addAll(Arrays.asList("刷新", "重置", "重载", "refresh", "reset", "reload"));
            for (String s : subs) if (s.startsWith(args[0])) out.add(s);
        } else if (args.length == 2 && (args[0].equals("领取") || args[0].equals("完成") || args[0].equals("重置"))) {
            for (String id : plugin.getQuestManager().all().keySet()) {
                if (id.startsWith(args[1].toLowerCase())) out.add(id);
            }
        }
        return out;
    }
}