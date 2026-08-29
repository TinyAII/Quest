package nl.tinyaii.quest.economy;

import nl.tinyaii.quest.QuestPlugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.lang.reflect.Method;

/**
 * Economy 反射联动（全家桶同款）：装了发金币，没装跳过。编译期零依赖。
 */
public class EcoBridge {

    private final QuestPlugin plugin;
    private boolean available;
    private Method mDeposit, mGetBalance;
    private String currencyName = "金币";

    public EcoBridge(QuestPlugin plugin) {
        this.plugin = plugin;
        if (Bukkit.getPluginManager().getPlugin("Economy") == null) {
            available = false;
            return;
        }
        try {
            Class<?> api = Class.forName("nl.tinyaii.economy.api.EconomyAPI");
            mDeposit = api.getMethod("deposit", java.util.UUID.class, double.class);
            mGetBalance = api.getMethod("getBalance", java.util.UUID.class);
            try {
                Plugin eco = Bukkit.getPluginManager().getPlugin("Economy");
                File cfgFile = new File(eco.getDataFolder(), "config.yml");
                if (cfgFile.exists()) {
                    org.bukkit.configuration.file.YamlConfiguration yml =
                            org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(cfgFile);
                    currencyName = yml.getString("settings.currency-name", "金币");
                }
            } catch (Throwable ignored) {}
            available = true;
            initPointsApi();
            plugin.getLogger().info("已检测到 Economy 插件，任务金币/点券奖励启用。");
        } catch (Throwable t) {
            plugin.getLogger().warning("Economy API 反射失败（金币奖励禁用）: " + t.getMessage());
            available = false;
        }
    }

    public boolean isAvailable() { return available; }

    public void deposit(java.util.UUID uuid, double amount) {
        if (!available || amount <= 0) return;
        try { mDeposit.invoke(null, uuid, amount); } catch (Exception ignored) {}
    }

    public String getCurrencyName() { return currencyName; }

    /** 发点券（Economy v2.0 点券 API，反射；未装则跳过） */
    private Method mDepositPoints;
    public void initPointsApi() {
        try {
            Class<?> api = Class.forName("nl.tinyaii.economy.api.EconomyAPI");
            mDepositPoints = api.getMethod("depositPoints", java.util.UUID.class, int.class);
        } catch (Throwable ignored) {}
    }
    public void depositPoints(java.util.UUID uuid, int amount) {
        if (amount <= 0 || mDepositPoints == null) return;
        try { mDepositPoints.invoke(null, uuid, amount); } catch (Exception ignored) {}
    }
}