package nl.tinyaii.quest.quest;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 任务模型：kill / mine / reach 三类目标 + 奖励 + 链式解锁。
 */
public class Quest {
    public enum Type { KILL, MINE, REACH }

    private final String id;
    private String name;
    private final List<String> description = new ArrayList<>();
    private Material icon = Material.BOOK;
    private Type type;
    private EntityType mob;          // KILL
    private Material block;          // MINE
    private boolean useSpawn;        // REACH：true=用世界出生点（每个服都有），false=显式坐标
    private String targetWorld;      // REACH
    private double targetX, targetY, targetZ;
    private double radius;
    private int amount;
    private double money;                                  // 经济套：金币
    private int points;                                    // 经济套：点券（Economy v2.0 第二币，可配）
    private int economyExp;                                // 经济套：经验（装了也发，量少）
    private final List<ItemStack> itemRewards = new ArrayList<>();   // 经济套：物品
    private int fallbackExp;                               // 经验套：经验（未装 Economy 时）
    private final List<ItemStack> fallbackItems = new ArrayList<>(); // 经验套：物品
    private final List<String> commandRewards = new ArrayList<>();   // 双模式共用命令
    private String next = "";        // 链式解锁的下一任务 id

    public Quest(String id) { this.id = id; }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String n) { this.name = n; }
    public List<String> getDescription() { return description; }
    public Material getIcon() { return icon; }
    public void setIcon(Material m) { this.icon = m; }
    public Type getType() { return type; }
    public void setType(Type t) { this.type = t; }
    public EntityType getMob() { return mob; }
    public void setMob(EntityType m) { this.mob = m; }
    public Material getBlock() { return block; }
    public void setBlock(Material m) { this.block = m; }
    public boolean isUseSpawn() { return useSpawn; }
    public void setUseSpawn(boolean b) { this.useSpawn = b; }
    public String getTargetWorld() { return targetWorld; }
    public double getTargetX() { return targetX; }
    public double getTargetY() { return targetY; }
    public double getTargetZ() { return targetZ; }
    public double getRadius() { return radius; }
    public void setTarget(Location loc, double r) {
        this.targetWorld = loc == null || loc.getWorld() == null ? "world" : loc.getWorld().getName();
        this.targetX = loc.getX(); this.targetY = loc.getY(); this.targetZ = loc.getZ();
        this.radius = r;
    }
    public int getAmount() { return amount; }
    public void setAmount(int a) { this.amount = Math.max(1, a); }
    public double getMoney() { return money; }
    public void setMoney(double m) { this.money = m; }
    public int getPoints() { return points; }
    public void setPoints(int p) { this.points = Math.max(0, p); }
    public int getEconomyExp() { return economyExp; }
    public void setEconomyExp(int e) { this.economyExp = Math.max(0, e); }
    public int getFallbackExp() { return fallbackExp; }
    public void setFallbackExp(int e) { this.fallbackExp = Math.max(0, e); }
    public List<ItemStack> getItemRewards() { return itemRewards; }
    public List<ItemStack> getFallbackItems() { return fallbackItems; }
    public List<String> getCommandRewards() { return commandRewards; }
    public String getNext() { return next; }
    public void setNext(String n) { this.next = n == null ? "" : n; }
}