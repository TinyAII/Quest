package nl.tinyaii.quest.gui;

import nl.tinyaii.quest.QuestPlugin;
import nl.tinyaii.quest.quest.Quest;
import nl.tinyaii.quest.quest.QuestProgress;
import nl.tinyaii.quest.util.Messages;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * 任务面板 GUI：
 * - 主入口页三分区（每日 / 进行中 / 主线）
 * - 每日分区：只显示今日随机抽中的 N 个（可领）+ 「任务池」入口（查看全部28个只读）
 * - 主线分区：章节选择页（五章）→ 点章节 → 该章 10 环任务
 * - 任务池：只读查看全部每日任务与奖励（不可领取）
 */
public class QuestMenu {

    public static final String TITLE = ChatColor.DARK_GRAY + "任务面板";
    private static final int SIZE = 27;
    private final QuestPlugin plugin;
    private final Player player;

    public QuestMenu(QuestPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(new QuestHolder("main", 0, 1), SIZE, TITLE);

        QuestProgress prog = plugin.getProgressManager().get(player.getUniqueId());
        int activeCount = prog.active.size();

        inv.setItem(11, named(Material.SUNFLOWER, "&e每日任务",
                new ArrayList<>(java.util.Arrays.asList(
                        Messages.color("&7每天随机抽取 " + plugin.getConfig().getInt("settings.daily-count", 3) + " 个任务"),
                        Messages.color("&7今日完成: &f" + prog.completedToday.size() + " 个"),
                        Messages.color(""), Messages.color("&e点击查看今日任务")))));

        inv.setItem(13, named(Material.BOOK, "&b进行中 (" + activeCount + ")",
                activeCount == 0 ? new ArrayList<>(java.util.Arrays.asList(Messages.color("&7暂无进行中的任务")))
                        : new ArrayList<>(java.util.Arrays.asList(Messages.color("&7查看当前任务进度")))));

        inv.setItem(15, named(Material.MAP, "&d主线任务链", new ArrayList<>(java.util.Arrays.asList(
                Messages.color("&7章节式推进，完成解锁下一环"),
                Messages.color("&7已完成: &f" + prog.completedChain.size() + " 环"),
                Messages.color(""), Messages.color("&e点击选择章节")))));

        player.openInventory(inv);
    }

    /**
     * 展开分区。
     * section: daily=今日任务 / pool=任务池(全部每日) / active=进行中 / chapters=主线章节选择 / chapter.<N>=第N章任务
     */
    public static void openSection(QuestPlugin plugin, Player player, String section, int page) {
        QuestProgress prog = plugin.getProgressManager().get(player.getUniqueId());

        // ---------- 每日任务：只显示今日抽中的 N 个 ----------
        if (section.equals("daily")) {
            plugin.getQuestService().pickDailyIfNeeded(player);
            List<String> picked = new ArrayList<>(prog.dailyPicked);
            // 补上已完成（防止当日已完成但仍在列表中被点开）
            for (String id : plugin.getQuestManager().dailyPool()) {
                if (!picked.contains(id) && prog.completedToday.contains(id)) picked.add(id);
            }
            List<Quest> list = new ArrayList<>();
            for (String id : picked) {
                Quest q = plugin.getQuestManager().get(id);
                if (q != null) list.add(q);
            }
            int pages = Math.max(1, (list.size() + 35) / 36);
            int p = Math.max(0, Math.min(page, pages - 1));
            Inventory inv = Bukkit.createInventory(new QuestHolder("daily", p, pages), 54,
                    ChatColor.DARK_GRAY + "今日任务（随机" + list.size() + "个）");

            for (int i = 0; i < 36; i++) {
                if (p * 36 + i >= list.size()) break;
                Quest q = list.get(p * 36 + i);
                inv.setItem(i, questIcon(plugin, player, q, prog));
            }
            // 底部导航 + 任务池入口
            ItemStack filler = named(Material.GRAY_STAINED_GLASS_PANE, " ", new ArrayList<>());
            for (int i = 45; i < 54; i++) inv.setItem(i, filler);
            if (p > 0) inv.setItem(48, named(Material.ARROW, "&e上一页", new ArrayList<>()));
            if (p < pages - 1) inv.setItem(50, named(Material.ARROW, "&e下一页", new ArrayList<>()));
            inv.setItem(49, named(Material.BARRIER, "&c返回任务面板", new ArrayList<>()));
            // 任务池入口（第47格）
            inv.setItem(47, named(Material.CHEST, "&b查看任务池",
                    new ArrayList<>(java.util.Arrays.asList(Messages.color("&7查看全部 " + plugin.getQuestManager().dailyPool().size() + " 个每日任务及奖励（只读）")))));
            // 一键领取今日全部（第46格）
            inv.setItem(46, named(Material.GOLD_INGOT, "&a一键领取今日全部",
                    new ArrayList<>(java.util.Arrays.asList(
                            Messages.color("&7一键接取今日 " + list.size() + " 个任务"),
                            Messages.color("&7（未开始的才会被领取）")))));
            player.openInventory(inv);
            return;
        }

        // ---------- 任务池：全部每日任务（只读，不可领取） ----------
        if (section.equals("pool")) {
            List<Quest> list = new ArrayList<>();
            for (String id : plugin.getQuestManager().dailyPool()) {
                Quest q = plugin.getQuestManager().get(id);
                if (q != null) list.add(q);
            }
            int pages = Math.max(1, (list.size() + 35) / 36);
            int p = Math.max(0, Math.min(page, pages - 1));
            Inventory inv = Bukkit.createInventory(new QuestHolder("pool", p, pages), 54,
                    ChatColor.DARK_GRAY + "任务池（全部 " + list.size() + " 个 · 只读）");
            for (int i = 0; i < 36; i++) {
                if (p * 36 + i >= list.size()) break;
                Quest q = list.get(p * 36 + i);
                inv.setItem(i, poolIcon(plugin, q));
            }
            ItemStack filler = named(Material.GRAY_STAINED_GLASS_PANE, " ", new ArrayList<>());
            for (int i = 45; i < 54; i++) inv.setItem(i, filler);
            if (p > 0) inv.setItem(48, named(Material.ARROW, "&e上一页", new ArrayList<>()));
            if (p < pages - 1) inv.setItem(50, named(Material.ARROW, "&e下一页", new ArrayList<>()));
            inv.setItem(49, named(Material.BARRIER, "&c返回任务面板", new ArrayList<>()));
            player.openInventory(inv);
            return;
        }

        // ---------- 进行中 ----------
        if (section.equals("active")) {
            List<Quest> list = new ArrayList<>();
            for (String id : prog.active.keySet()) {
                Quest q = plugin.getQuestManager().get(id);
                if (q != null) list.add(q);
            }
            int pages = Math.max(1, (list.size() + 35) / 36);
            int p = Math.max(0, Math.min(page, pages - 1));
            Inventory inv = Bukkit.createInventory(new QuestHolder("active", p, pages), 54,
                    ChatColor.DARK_GRAY + "进行中任务");
            for (int i = 0; i < 36; i++) {
                if (p * 36 + i >= list.size()) break;
                Quest q = list.get(p * 36 + i);
                inv.setItem(i, questIcon(plugin, player, q, prog));
            }
            ItemStack filler = named(Material.GRAY_STAINED_GLASS_PANE, " ", new ArrayList<>());
            for (int i = 45; i < 54; i++) inv.setItem(i, filler);
            if (p > 0) inv.setItem(48, named(Material.ARROW, "&e上一页", new ArrayList<>()));
            if (p < pages - 1) inv.setItem(50, named(Material.ARROW, "&e下一页", new ArrayList<>()));
            inv.setItem(49, named(Material.BARRIER, "&c返回任务面板", new ArrayList<>()));
            player.openInventory(inv);
            return;
        }

        // ---------- 主线：章节选择页 ----------
        if (section.equals("chapters")) {
            Inventory inv = Bukkit.createInventory(new QuestHolder("chapters", 0, 1), 27,
                    ChatColor.DARK_GRAY + "主线章节");
            String[][] chapters = chapterMeta();
            int[] slots = {11, 12, 13, 14, 15};
            for (int i = 0; i < chapters.length && i < slots.length; i++) {
                String chId = "chapter." + (i + 1);
                int done = countChainDone(plugin, player, i);
                inv.setItem(slots[i], named(Material.BOOKSHELF, chapters[i][0],
                        new ArrayList<>(java.util.Arrays.asList(
                                Messages.color("&7" + chapters[i][1]),
                                Messages.color("&7已完成: &f" + done + " &7/ 10"),
                                Messages.color(""), Messages.color("&e点击查看本章任务")))));
                // 用 invisible marker 记录章节号：Lore 尾部追加隐藏文本
                var meta = inv.getItem(slots[i]).getItemMeta();
                List<String> lore = meta.getLore();
                lore.add(ChatColor.BLACK + "" + ChatColor.DARK_GRAY + "CH" + (i + 1));
                meta.setLore(lore);
                inv.getItem(slots[i]).setItemMeta(meta);
            }
            inv.setItem(22, named(Material.ARROW, "&e返回任务面板", new ArrayList<>()));
            player.openInventory(inv);
            return;
        }

        // ---------- 主线：某章节任务页 ----------
        if (section.startsWith("chapter.")) {
            int chIdx;
            try { chIdx = Integer.parseInt(section.substring(8)); } catch (Exception e) { chIdx = 1; }
            List<String> chapterIds = chapterQuestIds(plugin, chIdx);
            List<Quest> list = new ArrayList<>();
            for (String id : chapterIds) {
                Quest q = plugin.getQuestManager().get(id);
                if (q != null) list.add(q);
            }
            int pages = Math.max(1, (list.size() + 35) / 36);
            int p = Math.max(0, Math.min(page, pages - 1));
            String[][] chapters = chapterMeta();
            Inventory inv = Bukkit.createInventory(new QuestHolder("chapter." + chIdx, p, pages), 54,
                    ChatColor.DARK_GRAY + chapters[chIdx - 1][0] + " 任务");
            for (int i = 0; i < 36; i++) {
                if (p * 36 + i >= list.size()) break;
                Quest q = list.get(p * 36 + i);
                inv.setItem(i, questIcon(plugin, player, q, prog));
            }
            ItemStack filler = named(Material.GRAY_STAINED_GLASS_PANE, " ", new ArrayList<>());
            for (int i = 45; i < 54; i++) inv.setItem(i, filler);
            if (p > 0) inv.setItem(48, named(Material.ARROW, "&e上一页", new ArrayList<>()));
            if (p < pages - 1) inv.setItem(50, named(Material.ARROW, "&e下一页", new ArrayList<>()));
            inv.setItem(49, named(Material.BARRIER, "&c返回章节选择", new ArrayList<>()));
            player.openInventory(inv);
            return;
        }
    }

    /** 五章元信息（名称+描述） */
    private static String[][] chapterMeta() {
        return new String[][]{
                {"&b第一章 · 初入世界", "徒手求生 · 牛皮起步"},
                {"&6第二章 · 铁与火", "铁器时代 · 铁装齐身"},
                {"&5第三章 · 钻光初现", "钻石前夕 · 第一颗钻"},
                {"&b第四章 · 钻石时代", "钻石全盛 · 点券首现"},
                {"&c第五章 · 终局之战", "毕业之章 · 合金剑锋"}
        };
    }

    /** 第 chIdx 章（1-5）的任务 id 列表（按链顺序截取 10 个） */
    private static List<String> chapterQuestIds(QuestPlugin plugin, int chIdx) {
        List<String> chain = plugin.getQuestManager().chainOrder();
        List<String> out = new ArrayList<>();
        int start = (chIdx - 1) * 10;
        for (int i = start; i < Math.min(start + 10, chain.size()); i++) out.add(chain.get(i));
        return out;
    }

    private static int countChainDone(QuestPlugin plugin, Player player, int chIdx) {
        QuestProgress prog = plugin.getProgressManager().get(player.getUniqueId());
        int done = 0;
        for (String id : chapterQuestIds(plugin, chIdx + 1)) {
            if (prog.completedChain.contains(id)) done++;
        }
        return done;
    }

    /** 任务图标（可领取/进行中/已完成/未解锁） */
    private static ItemStack questIcon(QuestPlugin plugin, Player player, Quest q, QuestProgress prog) {
        ItemStack icon = new ItemStack(q.getIcon());
        ItemMeta meta = icon.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Messages.color(q.getName()));
            List<String> lore = new ArrayList<>();
            for (String l : q.getDescription()) lore.add(Messages.color(l));
            lore.add("");

            boolean isDaily = plugin.getQuestManager().dailyPool().contains(q.getId());
            boolean isChain = plugin.getQuestManager().chainOrder().contains(q.getId());
            Integer cur = prog.active.get(q.getId());

            if (cur != null) {
                lore.add(Messages.color("&6进行中: &f" + cur + " / " + q.getAmount()));
                lore.add("");
                if (plugin.getConfig().getBoolean("settings.auto-complete", true)) {
                    lore.add(Messages.color("&a达成目标自动完成"));
                } else {
                    lore.add(Messages.color("&e达成目标后 &f/任务 完成 " + q.getId()));
                }
            } else if (isDaily && prog.completedToday.contains(q.getId())) {
                lore.add(Messages.color("&a✔ 今日已完成"));
            } else if (isChain && prog.completedChain.contains(q.getId())) {
                lore.add(Messages.color("&a✔ 已完成"));
            } else if (isChain && !chainUnlocked(plugin, q)) {
                lore.add(Messages.color("&c未解锁（先完成前置任务）"));
            } else if (isDaily && !prog.dailyPicked.contains(q.getId())) {
                lore.add(Messages.color("&7今日未抽中（随机任务）"));
            } else {
                lore.add(Messages.color("&e左键领取"));
            }

            String reward = rewardText(plugin, q);
            if (!reward.isEmpty()) {
                lore.add("");
                lore.add(Messages.color("&7奖励: &f" + reward));
            }
            meta.setLore(lore);
            icon.setItemMeta(meta);
        }
        return icon;
    }

    /** 任务池图标（只读：全部显示，标"任务池"而非可领取） */
    private static ItemStack poolIcon(QuestPlugin plugin, Quest q) {
        ItemStack icon = new ItemStack(q.getIcon());
        ItemMeta meta = icon.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Messages.color("&7" + q.getName().replace("&c", "").replace("&e", "").replace("&b", "").replace("&f", "")));
            List<String> lore = new ArrayList<>();
            for (String l : q.getDescription()) lore.add(Messages.color(l));
            lore.add("");
            lore.add(Messages.color("&7任务池查看（今日未抽中）"));
            String reward = rewardText(plugin, q);
            if (!reward.isEmpty()) {
                lore.add("");
                lore.add(Messages.color("&7奖励: &f" + reward));
            }
            meta.setLore(lore);
            icon.setItemMeta(meta);
        }
        return icon;
    }

    private static boolean chainUnlocked(QuestPlugin plugin, Quest q) {
        java.util.List<String> chain = plugin.getQuestManager().chainOrder();
        int idx = chain.indexOf(q.getId());
        if (idx <= 0) return true;
        return true;   // 领取时由 QuestService 判定
    }

    private static String rewardText(QuestPlugin plugin, Quest q) {
        StringBuilder sb = new StringBuilder();
        boolean economyOn = plugin.getEcoBridge().isAvailable();
        if (economyOn) {
            if (q.getMoney() > 0) sb.append(q.getMoney()).append(" ").append(plugin.getEcoBridge().getCurrencyName());
            if (q.getPoints() > 0) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(q.getPoints()).append(" 点券");
            }
            for (ItemStack it : q.getItemRewards()) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(it.getAmount()).append("x ").append(nl.tinyaii.quest.util.MaterialNames.name(it));
            }
        } else {
            if (q.getFallbackExp() > 0) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(q.getFallbackExp()).append(" 经验");
            }
            for (ItemStack it : q.getFallbackItems()) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(it.getAmount()).append("x ").append(nl.tinyaii.quest.util.MaterialNames.name(it));
            }
        }
        return sb.toString();
    }

    private static ItemStack named(Material mat, String name, List<String> lore) {
        ItemStack it = new ItemStack(mat);
        ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Messages.color(name));
            List<String> out = new ArrayList<>();
            for (String l : lore) out.add(Messages.color(l));
            meta.setLore(out);
            it.setItemMeta(meta);
        }
        return it;
    }
}
