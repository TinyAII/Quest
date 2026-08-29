package nl.tinyaii.quest.gui;

import nl.tinyaii.quest.QuestPlugin;
import nl.tinyaii.quest.quest.Quest;
import nl.tinyaii.quest.quest.QuestProgress;
import nl.tinyaii.quest.quest.QuestService;
import nl.tinyaii.quest.util.Messages;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 任务 GUI 点击：进入分区 / 领取任务 / 翻页。
 */
public class MenuListener implements Listener {

    private final QuestPlugin plugin;
    private final java.util.Map<java.util.UUID, Long> lastClick = new java.util.HashMap<>();

    public MenuListener(QuestPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof QuestHolder)) return;
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();
        e.setCancelled(true);
        if (e.getClickedInventory() != e.getView().getTopInventory()) return;
        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) return;
        if (e.getClick() != ClickType.LEFT && e.getClick() != ClickType.SHIFT_LEFT) return;

        // 防抖 300ms
        long now = System.currentTimeMillis();
        Long last = lastClick.get(p.getUniqueId());
        if (last != null && now - last < 300) return;
        lastClick.put(p.getUniqueId(), now);

        QuestHolder holder = (QuestHolder) e.getInventory().getHolder();
        int slot = e.getRawSlot();

        // ---- 主入口页（27格）----
        if (holder.getSection().equals("main")) {
            switch (slot) {
                case 11 -> QuestMenu.openSection(plugin, p, "daily", 0);
                case 13 -> QuestMenu.openSection(plugin, p, "active", 0);
                case 15 -> QuestMenu.openSection(plugin, p, "chapters", 0);
            }
            return;
        }

        // ---- 分区页（54格）----
        String section = holder.getSection();
        if (slot == 49) {
            // chapter.N → 章节选择页；其余 → 主面板
            if (section.startsWith("chapter.")) QuestMenu.openSection(plugin, p, "chapters", 0);
            else new QuestMenu(plugin, p).open();
            return;
        }
        if (slot == 48 && holder.getPage() > 0) { QuestMenu.openSection(plugin, p, section, holder.getPage() - 1); return; }
        if (slot == 50 && holder.getPage() < holder.getPages() - 1) { QuestMenu.openSection(plugin, p, section, holder.getPage() + 1); return; }

        // 每日分区：第47格 → 任务池
        if (section.equals("daily") && slot == 47) {
            QuestMenu.openSection(plugin, p, "pool", 0);
            return;
        }

        // 章节选择页（27格）：22=返回；点章节图标（lore 尾部 CH<章号>）→ 章节任务页
        if (section.equals("chapters")) {
            if (slot == 22) { new QuestMenu(plugin, p).open(); return; }
            String chTag = chapterTag(clicked);
            if (chTag != null) {
                QuestMenu.openSection(plugin, p, "chapter." + chTag, 0);
            }
            return;
        }

        // 每日分区：46号 = 一键领取今日全部
        if (section.equals("daily") && slot == 46) {
            p.closeInventory();
            int got = claimAllDaily(plugin, p);
            QuestMenu.openSection(plugin, p, "daily", holder.getPage());
            if (got > 0) p.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&',
                    "&a已一键领取 &e" + got + " &a个今日任务！"));
            return;
        }

        if (slot >= 36) return;
        // 从格子反查任务
        List<Quest> list = sectionList(section, p);
        int idx = holder.getPage() * 36 + slot;
        if (idx >= list.size()) return;
        Quest q = list.get(idx);

        QuestProgress prog = plugin.getProgressManager().get(p.getUniqueId());
        Integer cur = prog.active.get(q.getId());
        if (cur == null) {
            // 未在进度 → 领取
            QuestService service = new QuestService(plugin);
            String err = service.startQuest(p, q.getId());
            if (err != null) {
                Messages msg = plugin.getMessages();
                switch (err) {
                    case "quest-not-found" -> msg.send(p, "quest-not-found", "{quest}", q.getId());
                    case "quest-already-active" -> msg.send(p, "quest-already-active");
                    case "quest-chain-locked" -> msg.send(p, "quest-chain-locked");
                    case "daily-not-available" -> p.sendMessage(Messages.color("&c今日任务尚未刷新，请等待或联系管理员。"));
                    case "quest-already-completed" -> p.sendMessage(Messages.color("&c该任务今日已完成。"));
                }
            } else {
                plugin.getMessages().send(p, "quest-started", "{quest}", q.getName());
            }
            QuestMenu.openSection(plugin, p, section, holder.getPage());
        }
        // 已有进度 => 点击仅刷新
    }

    private List<Quest> sectionList(String section, Player p) {
        List<Quest> list = new ArrayList<>();
        if (section.equals("daily")) {
            for (String id : plugin.getQuestManager().dailyPool()) {
                Quest q = plugin.getQuestManager().get(id);
                if (q != null) list.add(q);
            }
        } else if (section.equals("chain")) {
            for (String id : plugin.getQuestManager().chainOrder()) {
                Quest q = plugin.getQuestManager().get(id);
                if (q != null) list.add(q);
            }
        } else if (section.equals("active")) {
            for (String id : plugin.getProgressManager().get(p.getUniqueId()).active.keySet()) {
                Quest q = plugin.getQuestManager().get(id);
                if (q != null) list.add(q);
            }
        }
        return list;
    }

    /** 一键领取今日全部未开始的每日任务 */
    private int claimAllDaily(QuestPlugin plugin, Player p) {
        plugin.getQuestService().pickDailyIfNeeded(p);
        int got = 0;
        for (String id : new java.util.ArrayList<>(plugin.getProgressManager().get(p.getUniqueId()).dailyPicked)) {
            var prog = plugin.getProgressManager().get(p.getUniqueId());
            if (prog.active.containsKey(id) || prog.completedToday.contains(id)) continue;
            String err = plugin.getQuestService().startQuest(p, id);
            if (err == null) got++;
        }
        return got;
    }

    /** 从章节图标 lore 尾部隐形标记提取章节号 */
    private String chapterTag(ItemStack item) {
        if (item.getItemMeta() == null || item.getItemMeta().getLore() == null) return null;
        for (String line : item.getItemMeta().getLore()) {
            String s = ChatColor.stripColor(line);
            if (s != null && s.startsWith("CH") && s.length() == 3) {
                try { return String.valueOf(Integer.parseInt(s.substring(2))); } catch (Exception ignored) {}
            }
        }
        return null;
    }
}
