package nl.tinyaii.quest.quest;

import nl.tinyaii.quest.QuestPlugin;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * 任务进度监听：杀怪/采集/到达三类目标实时计数。
 */
public class QuestListener implements Listener {

    private final QuestPlugin plugin;
    private final QuestService service;

    public QuestListener(QuestPlugin plugin, QuestService service) {
        this.plugin = plugin;
        this.service = service;
    }

    @EventHandler
    public void onKill(EntityDeathEvent e) {
        LivingEntity dead = e.getEntity();
        Player killer = dead.getKiller();
        if (killer == null) return;
        if (!plugin.getConfig().getBoolean("settings.auto-complete", true) && !hasActiveKill(killer)) return;

        QuestProgress prog = plugin.getProgressManager().get(killer.getUniqueId());
        for (String id : new java.util.ArrayList<>(prog.active.keySet())) {
            Quest q = plugin.getQuestManager().get(id);
            if (q == null || q.getType() != Quest.Type.KILL) continue;
            if (q.getMob() == dead.getType()) {
                service.addProgress(killer, id, 1);
            }
        }
    }

    @EventHandler
    public void onMine(BlockBreakEvent e) {
        Player p = e.getPlayer();
        Material broken = e.getBlock().getType();
        QuestProgress prog = plugin.getProgressManager().get(p.getUniqueId());
        for (String id : new java.util.ArrayList<>(prog.active.keySet())) {
            Quest q = plugin.getQuestManager().get(id);
            if (q == null || q.getType() != Quest.Type.MINE) continue;
            if (q.getBlock() == broken) {
                service.addProgress(p, id, 1);
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        // 只查方块级坐标变化，省性能
        if (e.getFrom().getBlockX() == e.getTo().getBlockX()
                && e.getFrom().getBlockY() == e.getTo().getBlockY()
                && e.getFrom().getBlockZ() == e.getTo().getBlockZ()) return;
        if (plugin.getProgressManager().get(e.getPlayer().getUniqueId()).active.isEmpty()) return;
        service.checkReach(e.getPlayer());
    }

    private boolean hasActiveKill(Player p) {
        QuestProgress prog = plugin.getProgressManager().get(p.getUniqueId());
        for (String id : prog.active.keySet()) {
            Quest q = plugin.getQuestManager().get(id);
            if (q != null && q.getType() == Quest.Type.KILL) return true;
        }
        return false;
    }

    // 引用避免未使用告警
    @SuppressWarnings("unused")
    private static final Enchantment UNUSED = null;
}