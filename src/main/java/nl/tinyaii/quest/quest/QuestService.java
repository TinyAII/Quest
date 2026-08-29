package nl.tinyaii.quest.quest;

import nl.tinyaii.quest.QuestPlugin;
import nl.tinyaii.quest.util.Messages;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * 任务服务：领取 / 进度 / 完成结算（奖金走 Economy，物品进背包，命令控制台执行）。
 */
public class QuestService {

    private final QuestPlugin plugin;

    public QuestService(QuestPlugin plugin) {
        this.plugin = plugin;
    }

    /** 领取任务。@return null=成功；否则返回错误消息 key */
    public String startQuest(Player p, String questId) {
        Quest q = plugin.getQuestManager().get(questId);
        if (q == null) return "quest-not-found";
        QuestProgress prog = plugin.getProgressManager().get(p.getUniqueId());

        // 每日任务：只可领取今日抽中的 3 个；完成记录按天隔离
        if (isDaily(q)) {
            if (prog.completedToday.contains(questId)) return "quest-already-completed";
            if (!prog.dailyPicked.contains(questId)) return "not-picked-today";
        }
        // 主线链：必须是链首或已完成前置
        if (isChain(q)) {
            String prev = prevOf(questId);
            if (prev != null && !prog.completedChain.contains(prev)) {
                return "quest-chain-locked";
            }
        }
        if (prog.active.containsKey(questId)) return "quest-already-active";

        prog.active.put(questId, 0);
        plugin.getProgressManager().save();
        return null;
    }

    /** 进度 +delta（杀怪/采集时调用） */
    public void addProgress(Player p, String questId, int delta) {
        QuestProgress prog = plugin.getProgressManager().get(p.getUniqueId());
        Integer cur = prog.active.get(questId);
        if (cur == null) return;
        Quest q = plugin.getQuestManager().get(questId);
        if (q == null) return;

        int next = Math.min(q.getAmount(), cur + delta);
        prog.active.put(questId, next);
        plugin.getProgressManager().save();

        if (next >= q.getAmount()) {
            if (plugin.getConfig().getBoolean("settings.auto-complete", true)) {
                completeQuest(p, questId);
            } else {
                plugin.getMessages().send(p, "quest-completed-hint", "{quest}", q.getName());
            }
        } else {
            // 进度提示显示在屏幕中下方 ActionBar（物品栏上方），不再刷聊天栏
            nl.tinyaii.quest.util.ActionBar.send(p,
                    Messages.color("&b" + q.getName() + " &7" + next + "/" + q.getAmount()
                            + " &8" + nl.tinyaii.quest.util.ActionBar.bar(next, q.getAmount())));
        }
    }

    /** 到达类：判定玩家位置命中则完成 */
    public void checkReach(Player p) {
        QuestProgress prog = plugin.getProgressManager().get(p.getUniqueId());
        for (String id : new java.util.ArrayList<>(prog.active.keySet())) {
            Quest q = plugin.getQuestManager().get(id);
            if (q == null || q.getType() != Quest.Type.REACH) continue;
            if (q.getAmount() <= 0) continue;
            if (!p.getWorld().getName().equals(q.getTargetWorld())) continue;
            double dx = p.getLocation().getX() - q.getTargetX();
            double dy = p.getLocation().getY() - q.getTargetY();
            double dz = p.getLocation().getZ() - q.getTargetZ();
            if (Math.sqrt(dx*dx + dy*dy + dz*dz) <= q.getRadius()) {
                prog.active.put(id, q.getAmount());
                plugin.getProgressManager().save();
                // 到达完成时也走 ActionBar 简洁提示
                nl.tinyaii.quest.util.ActionBar.send(p,
                        Messages.color("&b" + q.getName() + " &7" + q.getAmount() + "/" + q.getAmount()
                                + " &8" + nl.tinyaii.quest.util.ActionBar.bar(q.getAmount(), q.getAmount())));
                if (plugin.getConfig().getBoolean("settings.auto-complete", true)) {
                    completeQuest(p, id);
                }
            }
        }
    }

    /** 手动提交（非自动完成模式） */
    public boolean claim(Player p, String questId) {
        QuestProgress prog = plugin.getProgressManager().get(p.getUniqueId());
        Integer cur = prog.active.get(questId);
        if (cur == null) return false;
        Quest q = plugin.getQuestManager().get(questId);
        if (q == null) return false;
        if (cur < q.getAmount()) {
            plugin.getMessages().send(p, "quest-claim-not-ready",
                    "{current}", String.valueOf(cur), "{target}", String.valueOf(q.getAmount()));
            return false;
        }
        return completeQuest(p, questId);
    }

    /** 结算（内部统一入口） */
    private boolean completeQuest(Player p, String questId) {
        QuestProgress prog = plugin.getProgressManager().get(p.getUniqueId());
        Quest q = plugin.getQuestManager().get(questId);
        if (q == null) return false;

        prog.active.remove(questId);
        if (isDaily(q)) prog.completedToday.add(questId);
        if (isChain(q)) prog.completedChain.add(questId);
        plugin.getProgressManager().save();

        // 发放奖励（双模式：装 Economy 走金币套；没装走经验套）
        StringBuilder rewardStr = new StringBuilder();
        boolean economyOn = plugin.getEcoBridge().isAvailable();
        if (economyOn) {
            if (q.getMoney() > 0) {
                plugin.getEcoBridge().deposit(p.getUniqueId(), q.getMoney());
                rewardStr.append(Messages.color("&e")).append((long)q.getMoney()).append(" ")
                        .append(plugin.getEcoBridge().getCurrencyName()).append(" ");
            }
            if (q.getPoints() > 0) {
                plugin.getEcoBridge().depositPoints(p.getUniqueId(), q.getPoints());
                rewardStr.append(Messages.color("&d")).append(q.getPoints()).append(" 点券 ");
            }
            if (q.getEconomyExp() > 0) {
                p.giveExp(q.getEconomyExp());
                rewardStr.append(Messages.color("&a")).append(q.getEconomyExp()).append(" 经验 ");
            }
            for (ItemStack it : q.getItemRewards()) {
                Map<Integer, ItemStack> overflow = p.getInventory().addItem(it.clone());
                for (ItemStack rest : overflow.values()) {
                    p.getWorld().dropItemNaturally(p.getLocation(), rest);
                }
                rewardStr.append(Messages.color("&f")).append(it.getAmount()).append("x ")
                        .append(nl.tinyaii.quest.util.MaterialNames.name(it)).append(" ");
            }
        } else {
            if (q.getFallbackExp() > 0) {
                p.giveExp(q.getFallbackExp());
                rewardStr.append(Messages.color("&a")).append(q.getFallbackExp()).append(" 经验 ");
            }
            for (ItemStack it : q.getFallbackItems()) {
                Map<Integer, ItemStack> overflow = p.getInventory().addItem(it.clone());
                for (ItemStack rest : overflow.values()) {
                    p.getWorld().dropItemNaturally(p.getLocation(), rest);
                }
                rewardStr.append(Messages.color("&f")).append(it.getAmount()).append("x ")
                        .append(nl.tinyaii.quest.util.MaterialNames.name(it)).append(" ");
            }
        }
        for (String cmd : q.getCommandRewards()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{player}", p.getName()));
        }

        plugin.getMessages().send(p, "quest-completed",
                "{quest}", q.getName(), "{reward}", rewardStr.toString().trim());

        // 链式解锁提示
        if (q.getNext() != null && !q.getNext().isEmpty()) {
            Quest nq = plugin.getQuestManager().get(q.getNext());
            if (nq != null) {
                plugin.getMessages().send(p, "quest-chain-unlocked", "{quest}", nq.getName());
                startQuest(p, q.getNext());
            }
        }
        return true;
    }

    // ---------- 工具 ----------

    public boolean isDaily(Quest q) { return plugin.getQuestManager().dailyPool().contains(q.getId()); }
    public boolean isChain(Quest q) { return plugin.getQuestManager().chainOrder().contains(q.getId()); }

    private String prevOf(String questId) {
        java.util.List<String> chain = plugin.getQuestManager().chainOrder();
        int idx = chain.indexOf(questId);
        if (idx <= 0) return null;
        return chain.get(idx - 1);
    }

    /** 每日抽取：跨日后随机抽 settings.daily-count 个进玩家 dailyPicked（幂等，同日只抽一次） */
    public void pickDailyIfNeeded(Player p) {
        QuestProgress prog = plugin.getProgressManager().get(p.getUniqueId());
        String today = java.time.LocalDate.now().toString();
        if (prog.dailyDate.equals(today) && !prog.dailyPicked.isEmpty()) return;
        // 跨日（或首次）：清旧记录并抽新
        prog.completedToday.clear();
        prog.dailyDate = today;
        prog.dailyPicked.clear();
        int n = plugin.getConfig().getInt("settings.daily-count", 3);
        java.util.List<String> pool = new java.util.ArrayList<>(plugin.getQuestManager().dailyPool());
        java.util.Collections.shuffle(pool);
        for (int i = 0; i < Math.min(n, pool.size()); i++) {
            prog.dailyPicked.add(pool.get(i));
        }
        plugin.getProgressManager().save();
    }

    /** 该每日任务是否在玩家今日抽中列表里 */
    public boolean isPickedToday(Player p, String questId) {
        QuestProgress prog = plugin.getProgressManager().get(p.getUniqueId());
        return prog.dailyPicked.contains(questId);
    }

    /** 当前进行中任务（供 GUI/侧边栏） */
    public java.util.List<Quest> activeQuests(Player p) {
        QuestProgress prog = plugin.getProgressManager().get(p.getUniqueId());
        java.util.List<Quest> out = new java.util.ArrayList<>();
        for (String id : prog.active.keySet()) {
            Quest q = plugin.getQuestManager().get(id);
            if (q != null) out.add(q);
        }
        return out;
    }
}