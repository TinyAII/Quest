package nl.tinyaii.quest.progress;

import nl.tinyaii.quest.QuestPlugin;
import nl.tinyaii.quest.quest.QuestProgress;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 玩家任务进度存储：data.yml 单入口锁，变动即落盘。
 */
public class ProgressManager {
    private final QuestPlugin plugin;
    private final Map<UUID, QuestProgress> data = new HashMap<>();
    private File file;
    private final Object lock = new Object();

    public ProgressManager(QuestPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        synchronized (lock) {
            data.clear();
            file = new File(plugin.getDataFolder(), "data.yml");
            if (!file.exists()) return;
            YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection root = yml.getConfigurationSection("players");
            if (root == null) return;
            for (String key : root.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    ConfigurationSection s = root.getConfigurationSection(key);
                    if (s == null) continue;
                    QuestProgress p = new QuestProgress();
                    p.dailyDate = s.getString("daily-date", "");
                    p.completedChain.addAll(s.getStringList("chain"));
                    p.completedToday.addAll(s.getStringList("today"));
                    p.dailyPicked.addAll(s.getStringList("daily-picked"));
                    p.autoPickDaily = s.getBoolean("auto-pick-daily", false);
                    for (String k : s.getStringList("active-list")) {
                        p.active.put(k, s.getInt("active." + k, 0));
                    }
                    data.put(uuid, p);
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }

    public void save() {
        synchronized (lock) {
            YamlConfiguration yml = new YamlConfiguration();
            for (Map.Entry<UUID, QuestProgress> e : data.entrySet()) {
                QuestProgress p = e.getValue();
                String base = "players." + e.getKey() + ".";
                yml.set(base + "daily-date", p.dailyDate);
                yml.set(base + "chain", new java.util.ArrayList<>(p.completedChain));
                yml.set(base + "today", new java.util.ArrayList<>(p.completedToday));
                yml.set(base + "daily-picked", new java.util.ArrayList<>(p.dailyPicked));
                yml.set(base + "auto-pick-daily", p.autoPickDaily);
                yml.set(base + "active-list", new java.util.ArrayList<>(p.active.keySet()));
                for (Map.Entry<String, Integer> a : p.active.entrySet()) {
                    yml.set(base + "active." + a.getKey(), a.getValue());
                }
            }
            try {
                if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
                yml.save(file);
            } catch (IOException ex) {
                plugin.getLogger().severe("保存 data.yml 失败: " + ex.getMessage());
            }
        }
    }

    public QuestProgress get(UUID uuid) {
        synchronized (lock) {
            return data.computeIfAbsent(uuid, u -> new QuestProgress());
        }
    }

    public void reset(UUID uuid) {
        synchronized (lock) {
            data.put(uuid, new QuestProgress());
            save();
        }
    }

    /** 新的一天：清理所有玩家过期的每日任务进度（保留进行中/主线） */
    public void clearDailyBefore(String newDate, java.util.Set<String> dailyIds) {
        synchronized (lock) {
            boolean changed = false;
            for (QuestProgress p : data.values()) {
                if (!newDate.equals(p.dailyDate)) {
                    for (String id : new java.util.ArrayList<>(p.active.keySet())) {
                        if (dailyIds.contains(id)) {
                            p.active.remove(id);
                            changed = true;
                        }
                    }
                    p.completedToday.clear();
                    p.dailyPicked.clear();
                    p.dailyDate = newDate;
                    changed = true;
                }
            }
            if (changed) save();
        }
    }
}