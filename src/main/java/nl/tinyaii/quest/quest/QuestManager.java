package nl.tinyaii.quest.quest;

import nl.tinyaii.quest.QuestPlugin;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务定义管理器：解析 quests.yml（每日池 + 主线链），缓存全部任务。
 */
public class QuestManager {
    private final QuestPlugin plugin;
    private final Map<String, Quest> all = new LinkedHashMap<>();
    private final List<String> dailyPool = new ArrayList<>();     // 每日池任务 id
    private final List<String> chainOrder = new ArrayList<>();    // 主线链任务 id（有序）

    public QuestManager(QuestPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        all.clear();
        dailyPool.clear();
        chainOrder.clear();

        File f = new File(plugin.getDataFolder(), "quests.yml");
        if (!f.exists()) plugin.saveResource("quests.yml", false);
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(f);

        // 每日任务池：优先独立文件 daily-pool.yml（28个大池），兼容旧 quests.yml 内嵌段
        File dailyFile = new File(plugin.getDataFolder(), "daily-pool.yml");
        if (dailyFile.exists()) {
            YamlConfiguration dYml = YamlConfiguration.loadConfiguration(dailyFile);
            ConfigurationSection daily = dYml.getConfigurationSection("daily-pool");
            if (daily != null) {
                for (String key : daily.getKeys(false)) {
                    Quest q = parse(daily.getConfigurationSection(key), key);
                    if (q == null) continue;
                    all.put(key, q);
                    dailyPool.add(key);
                }
            }
        }
        ConfigurationSection daily = yml.getConfigurationSection("daily-pool");
        if (daily != null) {
            for (String key : daily.getKeys(false)) {
                Quest q = parse(daily.getConfigurationSection(key), key);
                if (q == null) continue;
                all.put(key, q);
                dailyPool.add(key);
            }
        }

        // 主线任务链
        ConfigurationSection chain = yml.getConfigurationSection("chain");
        if (chain != null) {
            for (String key : chain.getKeys(false)) {
                Quest q = parse(chain.getConfigurationSection(key), key);
                if (q == null) continue;
                all.put(key, q);
                chainOrder.add(key);
            }
        }

        plugin.getLogger().info("任务系统加载完成：每日 " + dailyPool.size() + " 个 | 主线 " + chainOrder.size() + " 个");
    }

    private Quest parse(ConfigurationSection s, String id) {
        if (s == null) return null;
        try {
            Quest q = new Quest(id);
            q.setName(s.getString("name", id));
            q.getDescription().addAll(s.getStringList("description"));
            Material icon = Material.matchMaterial(s.getString("icon", "BOOK"));
            if (icon != null) q.setIcon(icon);

            String type = s.getString("type", "kill").toLowerCase();
            if (type.equals("mine")) {
                q.setType(Quest.Type.MINE);
                Material b = Material.matchMaterial(s.getString("block", "STONE"));
                if (b == null) { plugin.getLogger().warning("任务 " + id + " 方块无效"); return null; }
                q.setBlock(b);
            } else if (type.equals("reach")) {
                q.setType(Quest.Type.REACH);
                boolean useSpawn = s.getBoolean("use-spawn", false);
                q.setUseSpawn(useSpawn);
                // reach 类任务没有 amount：完成判定不依赖计数，缺省记 1（"到达"即完成）
                q.setAmount(s.contains("amount") ? s.getInt("amount") : 1);
                if (useSpawn) {
                    String w = s.getString("world", "world");
                    org.bukkit.World world = org.bukkit.Bukkit.getWorld(w);
                    if (world == null) world = plugin.getServer().getWorlds().get(0);
                    org.bukkit.Location l = world == null ? new org.bukkit.Location(null, 0, 100, 0) : world.getSpawnLocation();
                    q.setTarget(l, s.getDouble("radius", 20));
                } else {
                    ConfigurationSection loc = s.getConfigurationSection("location");
                    if (loc == null) { plugin.getLogger().warning("任务 " + id + " 缺 location（或 use-spawn: true）"); return null; }
                    String w = loc.getString("world", "world");
                    org.bukkit.World world = org.bukkit.Bukkit.getWorld(w);
                    if (world == null) world = plugin.getServer().getWorlds().get(0);
                    org.bukkit.Location l = new org.bukkit.Location(world,
                            loc.getDouble("x", 0), loc.getDouble("y", 100), loc.getDouble("z", 0));
                    q.setTarget(l, loc.getDouble("radius", 10));
                }
            } else {
                q.setType(Quest.Type.KILL);
                try {
                    q.setMob(EntityType.valueOf(s.getString("mob", "ZOMBIE").toUpperCase()));
                } catch (Exception e) {
                    plugin.getLogger().warning("任务 " + id + " 怪物无效"); return null;
                }
            }
            q.setAmount(s.getInt("amount", 1));

            // 奖励：双模式
            //   economy  = 装了 Economy 时（金币+物品）
            //   fallback = 没装 Economy 时（经验+物品）
            //   兼容旧格式：rewards.money / rewards.items 顶层 = 经济套
            ConfigurationSection r = s.getConfigurationSection("rewards");
            if (r != null) {
                double ecoMoney = 0;
                List<ItemStack> ecoItems = new ArrayList<>();
                int fbExp = 0;
                List<ItemStack> fbItems = new ArrayList<>();

                ConfigurationSection eco = r.getConfigurationSection("economy");
                ConfigurationSection fb = r.getConfigurationSection("fallback");
                int ecoExp = 0;
                int ecoPoints = 0;
                if (eco != null) {
                    ecoMoney = eco.getDouble("money", 0);
                    ecoPoints = eco.getInt("points", 0);
                    ecoExp = eco.getInt("exp", 0);
                    ecoItems = parseItems(eco);
                } else {
                    // 旧格式：顶层 money/items/points
                    ecoMoney = r.getDouble("money", 0);
                    ecoPoints = r.getInt("points", 0);
                    ecoItems = parseItems(r);
                }
                if (fb != null) {
                    fbExp = fb.getInt("exp", 0);
                    fbItems = parseItems(fb);
                }

                q.setMoney(ecoMoney);
                q.setPoints(ecoPoints);
                q.setEconomyExp(ecoExp);
                q.getItemRewards().addAll(ecoItems);
                q.setFallbackExp(fbExp);
                q.getFallbackItems().addAll(fbItems);
                q.getCommandRewards().addAll(r.getStringList("commands"));
            }
            q.setNext(s.getString("next", ""));
            return q;
        } catch (Exception e) {
            plugin.getLogger().warning("解析任务 " + id + " 失败: " + e.getMessage());
            return null;
        }
    }

    private List<ItemStack> parseItems(ConfigurationSection sec) {
        List<ItemStack> out = new ArrayList<>();
        for (Map<?, ?> m : sec.getMapList("items")) {
            Object mat = m.get("material");
            if (mat == null) continue;
            Material mm = Material.matchMaterial(mat.toString());
            if (mm == null) continue;
            int amt = 1;
            Object a = m.get("amount");
            if (a instanceof Number) amt = ((Number) a).intValue();
            out.add(new ItemStack(mm, Math.max(1, amt)));
        }
        return out;
    }

    // ---------- 查询 ----------

    public Quest get(String id) { return all.get(id); }
    public Map<String, Quest> all() { return all; }
    public List<String> dailyPool() { return dailyPool; }
    public List<String> chainOrder() { return chainOrder; }

    /** 随机抽取 N 个每日任务（不重复） */
    public List<Quest> randomDaily(int n) {
        List<String> pool = new ArrayList<>(dailyPool);
        Collections.shuffle(pool);
        List<Quest> out = new ArrayList<>();
        for (int i = 0; i < Math.min(n, pool.size()); i++) {
            out.add(all.get(pool.get(i)));
        }
        return out;
    }
}