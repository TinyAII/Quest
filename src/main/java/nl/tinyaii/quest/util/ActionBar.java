package nl.tinyaii.quest.util;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

/**
 * ActionBar 发送工具（跨版本安全）：
 * 优先反射调用 Player.sendActionBar(String)（Paper/1.17+），
 * 1.16 Spigot 无该 API 时回退到 spoof 字符串显示（spigot().sendMessage 需要 bungee chat 类，
 * 为避免额外依赖，1.16 回退为 Chat 消息）。
 */
public final class ActionBar {
    private static Method SEND_ACTION_BAR;

    private ActionBar() {}

    static {
        try {
            SEND_ACTION_BAR = Player.class.getMethod("sendActionBar", String.class);
        } catch (NoSuchMethodException ignored) {
            SEND_ACTION_BAR = null;
        }
    }

    public static void send(Player p, String message) {
        send(p, message, false);
    }

    /**
     * @param prefix 是否自动加消息前缀（由调用方决定样式，默认不加）
     */
    public static void send(Player p, String message, boolean prefix) {
        if (p == null || message == null) return;
        String text = prefix ? message : message;
        if (SEND_ACTION_BAR != null) {
            try {
                SEND_ACTION_BAR.invoke(p, text);
                return;
            } catch (Exception ignored) {}
        }
        // 1.16 回退：Chat 消息（无法真正显示 ActionBar，但至少可见）
        p.sendMessage(text);
    }

    /** 进度条：用于任务进度展示（简洁紧凑） */
    public static String bar(int current, int target) {
        int total = 10;
        int filled = Math.round((float) Math.max(0, Math.min(current, target)) / Math.max(1, target) * total);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < total; i++) {
            if (i < filled) sb.append(ChatColor.GREEN).append("■");
            else sb.append(ChatColor.GRAY).append("□");
        }
        return sb.toString();
    }
}