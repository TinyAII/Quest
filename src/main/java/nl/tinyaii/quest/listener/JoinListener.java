package nl.tinyaii.quest.listener;

import nl.tinyaii.quest.QuestPlugin;
import nl.tinyaii.quest.quest.QuestProgress;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * 进服自动领取每日任务（玩家开启 autoPickDaily 后生效）。
 */
public class JoinListener implements Listener {

    private final QuestPlugin plugin;

    public JoinListener(QuestPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        QuestProgress prog = plugin.getProgressManager().get(p.getUniqueId());
        if (!prog.autoPickDaily) return;

        plugin.getQuestService().pickDailyIfNeeded(p);
        int got = 0;
        for (String id : new java.util.ArrayList<>(prog.dailyPicked)) {
            if (prog.active.containsKey(id) || prog.completedToday.contains(id)) continue;
            String err = plugin.getQuestService().startQuest(p, id);
            if (err == null) got++;
        }
        if (got > 0) {
            p.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&',
                    "&7[&b任务&7] &a已自动领取 &e" + got + " &a个今日任务！输入 &2/任务 &a查看。"));
        }
    }
}
