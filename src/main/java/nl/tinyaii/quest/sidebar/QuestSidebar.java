package nl.tinyaii.quest.sidebar;

import nl.tinyaii.quest.QuestPlugin;
import nl.tinyaii.quest.quest.Quest;
import nl.tinyaii.quest.quest.QuestProgress;
import nl.tinyaii.quest.quest.QuestService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

/**
 * 任务进度侧边栏：实时显示进行中任务进度（右侧计分板，低开销每20tick刷新）。
 */
public class QuestSidebar {

    private final QuestPlugin plugin;
    private final QuestService service;
    private org.bukkit.scheduler.BukkitTask task;

    public QuestSidebar(QuestPlugin plugin, QuestService service) {
        this.plugin = plugin;
        this.service = service;
    }

    public void start() {
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::updateAll, 40L, 20L);
    }

    public void stop() {
        if (task != null) task.cancel();
    }

    private void updateAll() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            update(p);
        }
    }

    private void update(Player p) {
        QuestProgress prog = plugin.getProgressManager().get(p.getUniqueId());
        ScoreboardManager mgr = Bukkit.getScoreboardManager();
        if (mgr == null) return;

        // 无进行中任务 → 清理旧侧边栏（避免残留显示 9/10 这类僵尸数字）
        if (prog.active.isEmpty()) {
            clearIfQuestSidebar(p, mgr);
            return;
        }

        Scoreboard board = mgr.getNewScoreboard();
        Objective obj = null;
        try { obj = board.registerNewObjective("quest", "dummy", ChatColor.AQUA + "◈ 任务中"); }
        catch (IllegalArgumentException e) { obj = board.getObjective("quest"); }
        if (obj == null) return;
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        int line = 0;
        for (String id : prog.active.keySet()) {
            Quest q = plugin.getQuestManager().get(id);
            if (q == null) continue;
            int cur = prog.active.get(id);
            // 重要：先转义 & 颜色码再取值——name 是 & 格式，直接塞计分板会把 &5 当字面量显示
            String raw = q.getName();
            String translated = ChatColor.translateAlternateColorCodes('&', raw);
            String stripped = ChatColor.stripColor(translated);
            if (stripped.length() > 24) stripped = stripped.substring(0, 24);
            String text = ChatColor.WHITE + "» " + stripped + ChatColor.GRAY + " " + cur + "/" + q.getAmount();
            obj.getScore(text).setScore(cur >= q.getAmount() ? q.getAmount() : cur);
            line++;
            if (line >= 10) break;   // 最多显示 10 行
        }
        p.setScoreboard(board);
    }

    /** 清掉我们之前设的"任务中"侧边栏（不影响其他插件计分板） */
    private void clearIfQuestSidebar(Player p, ScoreboardManager mgr) {
        Scoreboard board = p.getScoreboard();
        if (board == null) return;
        Objective obj = board.getObjective(DisplaySlot.SIDEBAR);
        // 只看我们自己的：含"任务中"标题的侧边栏
        if (obj != null && obj.getDisplayName() != null
                && obj.getDisplayName().contains("任务中")) {
            try {
                obj.unregister();
            } catch (Exception ignored) {}
        }
    }
}