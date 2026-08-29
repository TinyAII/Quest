package nl.tinyaii.quest.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * 材质中文名映射：GUI 与消息统一走这里；未收录的回退英文美化名。
 */
public final class MaterialNames {
    private MaterialNames() {}

    private static final java.util.Map<String, String> ZH = new java.util.HashMap<>();

    private static void put(String k, String v) { ZH.put(k.toUpperCase(), v); }

    static {
        // ===== 矿物 / 金属 =====
        put("COAL", "煤炭"); put("CHARCOAL", "木炭"); put("IRON_INGOT", "铁锭"); put("GOLD_INGOT", "金锭");
        put("COPPER_INGOT", "铜锭"); put("NETHERITE_INGOT", "下界合金锭"); put("DIAMOND", "钻石");
        put("EMERALD", "绿宝石"); put("REDSTONE", "红石粉"); put("LAPIS_LAZULI", "青金石");
        put("QUARTZ", "下界石英"); put("AMETHYST_SHARD", "紫水晶碎片");
        put("RAW_IRON", "粗铁"); put("RAW_GOLD", "粗金"); put("RAW_COPPER", "粗铜");
        put("IRON_NUGGET", "铁粒"); put("GOLD_NUGGET", "金粒");
        put("COAL_BLOCK", "煤炭块"); put("IRON_BLOCK", "铁块"); put("GOLD_BLOCK", "金块");
        put("DIAMOND_BLOCK", "钻石块"); put("EMERALD_BLOCK", "绿宝石块");
        put("REDSTONE_BLOCK", "红石块"); put("LAPIS_BLOCK", "青金石块"); put("COPPER_BLOCK", "铜块");

        // ===== 食物 =====
        put("BREAD", "面包"); put("APPLE", "苹果"); put("GOLDEN_APPLE", "金苹果");
        put("ENCHANTED_GOLDEN_APPLE", "附魔金苹果");
        put("BEEF", "生牛肉"); put("COOKED_BEEF", "牛排");
        put("PORKCHOP", "生猪排"); put("COOKED_PORKCHOP", "熟猪排");
        put("CHICKEN", "生鸡肉"); put("COOKED_CHICKEN", "熟鸡肉");
        put("MUTTON", "生羊肉"); put("COOKED_MUTTON", "熟羊肉");
        put("RABBIT", "生兔肉"); put("COOKED_RABBIT", "熟兔肉");
        put("COD", "生鳕鱼"); put("COOKED_COD", "熟鳕鱼");
        put("SALMON", "生鲑鱼"); put("COOKED_SALMON", "熟鲑鱼");
        put("TROPICAL_FISH", "热带鱼"); put("PUFFERFISH", "河豚");
        put("CARROT", "胡萝卜"); put("GOLDEN_CARROT", "金萝卜");
        put("POTATO", "马铃薯"); put("BAKED_POTATO", "烤马铃薯"); put("POISONOUS_POTATO", "毒马铃薯");
        put("BEETROOT", "甜菜根"); put("BEETROOT_SOUP", "甜菜汤");
        put("WHEAT", "小麦"); put("WHEAT_SEEDS", "小麦种子");
        put("PUMPKIN", "南瓜"); put("MELON", "西瓜"); put("MELON_SLICE", "西瓜片");
        put("SWEET_BERRIES", "甜浆果"); put("GLOW_BERRIES", "发光浆果");
        put("COOKIE", "曲奇"); put("CAKE", "蛋糕"); put("PUMPKIN_PIE", "南瓜派");
        put("SUGAR", "糖"); put("COCOA_BEANS", "可可豆"); put("EGG", "鸡蛋");
        put("MUSHROOM_STEW", "蘑菇煲"); put("RABBIT_STEW", "兔肉煲");
        put("HONEY_BOTTLE", "蜂蜜瓶"); put("HONEYCOMB", "蜜脾");
        put("DRIED_KELP", "干海带"); put("SUSPICIOUS_STEW", "迷之炖菜");

        // ===== 方块-建筑 =====
        put("STONE", "石头"); put("COBBLESTONE", "圆石"); put("MOSSY_COBBLESTONE", "苔石");
        put("STONE_BRICKS", "石砖"); put("GRANITE", "花岗岩"); put("DIORITE", "闪长岩");
        put("ANDESITE", "安山岩"); put("DEEPSLATE", "深板岩"); put("CALCITE", "方解石");
        put("DIRT", "泥土"); put("GRASS_BLOCK", "草方块"); put("PODZOL", "灰化土");
        put("SAND", "沙子"); put("RED_SAND", "红沙"); put("GRAVEL", "砂砾");
        put("CLAY", "黏土块"); put("TERRACOTTA", "陶瓦");
        put("OAK_LOG", "橡木原木"); put("SPRUCE_LOG", "云杉原木"); put("BIRCH_LOG", "白桦原木");
        put("JUNGLE_LOG", "丛林原木"); put("ACACIA_LOG", "金合欢原木"); put("DARK_OAK_LOG", "深色橡木原木");
        put("OAK_PLANKS", "橡木木板"); put("SPRUCE_PLANKS", "云杉木板"); put("BIRCH_PLANKS", "白桦木板");
        put("STICK", "木棍");
        put("GLASS", "玻璃"); put("OBSIDIAN", "黑曜石"); put("CRYING_OBSIDIAN", "哭泣的黑曜石");
        put("BEDROCK", "基岩"); put("NETHERRACK", "下界岩"); put("END_STONE", "末地石");
        put("GLOWSTONE", "萤石"); put("SEA_LANTERN", "海晶灯"); put("SHROOMLIGHT", "菌光体");
        put("SNOWBALL", "雪球"); put("SNOW_BLOCK", "雪块"); put("ICE", "冰"); put("PACKED_ICE", "浮冰");
        put("BRICKS", "红砖块"); put("BOOKSHELF", "书架"); put("CRAFTING_TABLE", "工作台");
        put("FURNACE", "熔炉"); put("BLAST_FURNACE", "高炉"); put("SMOKER", "烟熏炉");
        put("CHEST", "箱子"); put("TRAPPED_CHEST", "陷阱箱"); put("BARREL", "木桶");
        put("ENDER_CHEST", "末影箱"); put("SHULKER_BOX", "潜影盒");
        put("TORCH", "火把"); put("SOUL_TORCH", "灵魂火把"); put("LADDER", "梯子");
        put("TNT", "TNT"); put("SPONGE", "海绵"); put("SLIME_BALL", "粘液球"); put("SLIME_BLOCK", "粘液块");
        put("HAY_BLOCK", "干草块"); put("BONE_BLOCK", "骨块"); put("DRAGON_EGG", "龙蛋");
        put("RESPAWN_ANCHOR", "重生锚"); put("LODESTONE", "磁石"); put("BELL", "钟");
        put("ANCIENT_DEBRIS", "远古残骸"); put("NETHER_STAR", "下界之星");

        // ===== 工具 / 武器 =====
        put("WOODEN_SWORD", "木剑"); put("STONE_SWORD", "石剑"); put("IRON_SWORD", "铁剑");
        put("GOLDEN_SWORD", "金剑"); put("DIAMOND_SWORD", "钻石剑"); put("NETHERITE_SWORD", "下界合金剑");
        put("WOODEN_PICKAXE", "木镐"); put("STONE_PICKAXE", "石镐"); put("IRON_PICKAXE", "铁镐");
        put("GOLDEN_PICKAXE", "金镐"); put("DIAMOND_PICKAXE", "钻石镐"); put("NETHERITE_PICKAXE", "下界合金镐");
        put("WOODEN_AXE", "木斧"); put("STONE_AXE", "石斧"); put("IRON_AXE", "铁斧");
        put("GOLDEN_AXE", "金斧"); put("DIAMOND_AXE", "钻石斧"); put("NETHERITE_AXE", "下界合金斧");
        put("WOODEN_SHOVEL", "木锹"); put("STONE_SHOVEL", "石锹"); put("IRON_SHOVEL", "铁锹");
        put("GOLDEN_SHOVEL", "金锹"); put("DIAMOND_SHOVEL", "钻石锹"); put("NETHERITE_SHOVEL", "下界合金锹");
        put("WOODEN_HOE", "木锄"); put("STONE_HOE", "石锄"); put("IRON_HOE", "铁锄");
        put("GOLDEN_HOE", "金锄"); put("DIAMOND_HOE", "钻石锄"); put("NETHERITE_HOE", "下界合金锄");
        put("BOW", "弓"); put("ARROW", "箭"); put("SPECTRAL_ARROW", "光灵箭");
        put("SHIELD", "盾牌"); put("FISHING_ROD", "钓鱼竿"); put("FLINT_AND_STEEL", "打火石");
        put("SHEARS", "剪刀"); put("COMPASS", "指南针"); put("CLOCK", "时钟");
        put("SPYGLASS", "望远镜"); put("LEAD", "拴绳"); put("NAME_TAG", "命名牌");

        // ===== 防具 =====
        put("LEATHER_HELMET", "皮革帽子"); put("LEATHER_CHESTPLATE", "皮革外套");
        put("LEATHER_LEGGINGS", "皮革裤子"); put("LEATHER_BOOTS", "皮革靴子");
        put("IRON_HELMET", "铁头盔"); put("IRON_CHESTPLATE", "铁胸甲");
        put("IRON_LEGGINGS", "铁护腿"); put("IRON_BOOTS", "铁靴子");
        put("GOLDEN_HELMET", "金头盔"); put("GOLDEN_CHESTPLATE", "金胸甲");
        put("GOLDEN_LEGGINGS", "金护腿"); put("GOLDEN_BOOTS", "金靴子");
        put("DIAMOND_HELMET", "钻石头盔"); put("DIAMOND_CHESTPLATE", "钻石胸甲");
        put("DIAMOND_LEGGINGS", "钻石护腿"); put("DIAMOND_BOOTS", "钻石靴子");
        put("NETHERITE_HELMET", "下界合金头盔"); put("NETHERITE_CHESTPLATE", "下界合金胸甲");
        put("NETHERITE_LEGGINGS", "下界合金护腿"); put("NETHERITE_BOOTS", "下界合金靴子");

        // ===== 材料 / 杂项 =====
        put("STRING", "线"); put("FEATHER", "羽毛"); put("FLINT", "燧石"); put("LEATHER", "皮革");
        put("GUNPOWDER", "火药"); put("BONE", "骨头"); put("BONE_MEAL", "骨粉");
        put("ROTTEN_FLESH", "腐肉"); put("SPIDER_EYE", "蜘蛛眼");
        put("BLAZE_ROD", "烈焰棒"); put("BLAZE_POWDER", "烈焰粉");
        put("ENDER_PEARL", "末影珍珠"); put("ENDER_EYE", "末影之眼");
        put("GHAST_TEAR", "恶魂之泪"); put("PHANTOM_MEMBRANE", "幻翼膜");
        put("PAPER", "纸"); put("BOOK", "书"); put("WRITABLE_BOOK", "书与笔");
        put("EXPERIENCE_BOTTLE", "经验瓶"); put("BUCKET", "铁桶");
        put("WATER_BUCKET", "水桶"); put("LAVA_BUCKET", "熔岩桶");
        put("RAIL", "铁轨"); put("POWERED_RAIL", "充能铁轨"); put("DETECTOR_RAIL", "探测铁轨");
        put("MINECART", "矿车"); put("SADDLE", "鞍");
        put("REPEATER", "红石中继器"); put("COMPARATOR", "红石比较器");
        put("PISTON", "活塞"); put("STICKY_PISTON", "粘性活塞");
        put("DISPENSER", "发射器"); put("DROPPER", "投掷器"); put("HOPPER", "漏斗");
        put("OBSERVER", "侦测器"); put("REDSTONE_LAMP", "红石灯");
        put("REDSTONE_TORCH", "红石火把"); put("LEVER", "拉杆");
        put("ITEM_FRAME", "物品展示框"); put("PAINTING", "画");
        put("ENCHANTED_BOOK", "附魔书"); put("TOTEM_OF_UNDYING", "不死图腾");
        put("TOUCH_OF_GOLD", "金苹果"); put("ELYTRA", "鞘翅");
    }

    /** 中文优先，无映射则返回英文美化名（Stone Pickaxe） */
    public static String name(Material m) {
        String zh = ZH.get(m.name());
        return zh != null ? zh : pretty(m.name());
    }

    /** 自定义名 > 中文 > 英文 */
    public static String name(ItemStack stack) {
        if (stack == null) return "?";
        var meta = stack.getItemMeta();
        if (meta != null && meta.hasDisplayName()) return meta.getDisplayName();
        return name(stack.getType());
    }

    /** 消息用：自动带 §f 白色，保证聊天里可读 */
    public static String coloredName(ItemStack stack) {
        String n = name(stack);
        return n.startsWith("§") ? n : ChatColor.WHITE + n;
    }

    public static String pretty(String material) {
        StringBuilder sb = new StringBuilder();
        for (String s : material.toLowerCase().split("_")) {
            if (!s.isEmpty()) sb.append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).append(' ');
        }
        return sb.toString().trim();
    }
}
