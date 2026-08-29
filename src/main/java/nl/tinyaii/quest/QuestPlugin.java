package nl.tinyaii.quest;

import nl.tinyaii.quest.command.QuestCommand;
import nl.tinyaii.quest.economy.EcoBridge;
import nl.tinyaii.quest.gui.MenuListener;
import nl.tinyaii.quest.progress.ProgressManager;
import nl.tinyaii.quest.quest.QuestListener;
import nl.tinyaii.quest.quest.QuestManager;
import nl.tinyaii.quest.quest.QuestService;
import nl.tinyaii.quest.sidebar.QuestSidebar;
import nl.tinyaii.quest.util.Messages;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import java.time.LocalDate;

public class QuestPlugin extends JavaPlugin {

    private QuestManager questManager;
    private ProgressManager progressManager;
    private EcoBridge ecoBridge;
    private Messages messages;
    private QuestService questService;
    private QuestSidebar sidebar;

    private String dailyDate = "";

    @Override
    public void onEnable() {
        // TinyAII 品牌横幅 —— 必须在所有初始化逻辑之前输出（与 AutoBackup 完全一致）
        getLogger().info(" _____ _                _    ___ ___");
        getLogger().info("|_   _(_)_ __  _   _   / \\  |_ _|_ _|");
        getLogger().info("  | | | | '_ \\| | | | / _ \\  | | | |");
        getLogger().info("  | | | | | | | |_| |/ ___ \\ | | | |");
        getLogger().info("  |_| |_|_| |_|\\__, /_/   \\_\\___|___|");
        getLogger().info("               |___/");
        getLogger().info("Quest 任务系统 v" + getDescription().getVersion() + " - TinyAII 出品");

        saveDefaultConfig();
        migrateConfig();
        messages = new Messages(this);
        questManager = new QuestManager(this);
        questManager.load();
        progressManager = new ProgressManager(this);
        progressManager.load();
        ecoBridge = new EcoBridge(this);
        questService = new QuestService(this);

        getServer().getPluginManager().registerEvents(new nl.tinyaii.quest.listener.JoinListener(this), this);

        getCommand("任务").setExecutor(new QuestCommand(this));
        getCommand("任务").setTabCompleter(new QuestCommand(this));
        getServer().getPluginManager().registerEvents(new QuestListener(this, questService), this);
        getServer().getPluginManager().registerEvents(new MenuListener(this), this);

        // 每日任务板：每天跨日自动刷新
        dailyDate = LocalDate.now().toString();
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            String today = LocalDate.now().toString();
            if (!today.equals(dailyDate)) {
                dailyDate = today;
                refreshDaily();
                getLogger().info("每日任务板已自动刷新（" + today + "）");
            }
        }, 1200L, 1200L);   // 每 60 秒检查一次跨日

        // 侧边栏（每 20 tick 刷新一次，低开销）
        if (getConfig().getBoolean("settings.sidebar-enabled", true)) {
            sidebar = new QuestSidebar(this, questService);
            sidebar.start();
        }

        getLogger().info("任务系统已启用。每日板数量: " + getConfig().getInt("settings.daily-count", 3)
                + " | 自动完成: " + getConfig().getBoolean("settings.auto-complete", true));
    }

    @Override
    public void onDisable() {
        if (progressManager != null) progressManager.save();
        if (sidebar != null) sidebar.stop();
    }

    /** 刷新每日任务板（标记今日日期，玩家每日任务进度按日期隔离） */
    public int refreshDaily() {
        // 每日任务板在 QuestManager 里是静态池；玩家侧以 dailyDate 区分"今日"，
        // 这里主要推进 dailyDate 并从进度中清理过期每日任务
        String today = LocalDate.now().toString();
        progressManager.clearDailyBefore(today, new java.util.HashSet<>(questManager.dailyPool()));
        return questManager.dailyPool().size();
    }

    public void reloadAll() {
        reloadConfig();
        migrateConfig();
        messages.reload();
        questManager.load();
        progressManager.load();
        refreshDaily();
        if (getConfig().getBoolean("settings.sidebar-enabled", true)) {
            if (sidebar == null) { sidebar = new QuestSidebar(this, questService); sidebar.start(); }
        } else if (sidebar != null) {
            sidebar.stop(); sidebar = null;
        }
    }

    /** 配置迁移：旧 config.yml 缺失的新键自动从内置默认合并（Shop 同款） */
    private void migrateConfig() {
        java.io.File f = new java.io.File(getDataFolder(), "config.yml");
        if (!f.exists()) return;
        org.bukkit.configuration.file.YamlConfiguration user =
                org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(f);
        java.io.InputStream defStream = getResource("config.yml");
        if (defStream == null) return;
        org.bukkit.configuration.file.YamlConfiguration def =
                org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(
                        new java.io.InputStreamReader(defStream, java.nio.charset.StandardCharsets.UTF_8));
        boolean changed = false;
        for (String key : def.getKeys(true)) {
            if (!user.contains(key)) { user.set(key, def.get(key)); changed = true; }
        }
        if (changed) {
            try { user.save(f); getLogger().info("config.yml 已自动补齐新版配置项。"); }
            catch (Exception e) { getLogger().warning("config.yml 迁移失败: " + e.getMessage()); }
        }
    }

    public QuestManager getQuestManager() { return questManager; }
    public ProgressManager getProgressManager() { return progressManager; }
    public EcoBridge getEcoBridge() { return ecoBridge; }
    public Messages getMessages() { return messages; }
    public QuestService getQuestService() { return questService; }
}