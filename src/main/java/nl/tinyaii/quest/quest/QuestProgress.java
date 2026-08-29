package nl.tinyaii.quest.quest;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 玩家任务进度：进行中任务计数 + 已完成记录。
 */
public class QuestProgress {
    /** 进行中任务：id → 当前进度数 */
    public final Map<String, Integer> active = new LinkedHashMap<>();
    /** 已完成的主线任务 id（链式解锁依据） */
    public final java.util.Set<String> completedChain = new java.util.HashSet<>();
    /** 今日已完成任务 id（每日任务防重复） */
    public final java.util.Set<String> completedToday = new java.util.HashSet<>();
    public String dailyDate = "";   // dailyDate: 当日已完成任务的日期标记
    public final java.util.Set<String> dailyPicked = new java.util.HashSet<>();  // 今日随机抽中的任务 id
    public boolean autoPickDaily = false;  // 自动领取今日每日任务（进服自动领）
}