# Quest 任务系统

配置驱动的任务系统：杀怪 / 采集 / 到达三类目标、每日任务池（28 个随机抽取）、五章主线任务链（50 环）、双奖励模式、双语指令。零依赖开箱即用。

![Version](https://img.shields.io/badge/version-1.1.0-blue) ![License](https://img.shields.io/badge/license-MIT-green) ![API](https://img.shields.io/badge/API-1.16%2B-orange)

## 功能特性

- **三类任务目标**：杀怪（指定生物计数）/ 采集（指定方块计数，作物需成熟）/ 到达（走到指定坐标或世界出生点）
- **每日任务池 28 个**：每天随机抽 3 个展示（每个玩家独立抽取），跨日自动重置；另有「任务池」入口查看全部任务与奖励（只读）
- **一键领取**：每日分区一键领取今日全部；`/任务 自动领取 开` 开启进服自动领
- **主线「勇者成长之路」五章 50 环**：章节选择页 → 章节任务页两级导航；链式解锁、奖励平滑递增（金币 20→30→45→60→80）
- **平衡总则**：经验必发 / 物品按需 / 金币稀发 / 点券大章节收官专属；钻石成品与下界合金套不作任务奖励（防超时代）
- **双奖励模式**：装了 TinyAII Economy 发金币+点券+经验+物品；没装自动切 fallback 轨（经验×1.5+同样物品），两条路线闭环
- **进度反馈**：杀怪/采集时屏幕中下方 ActionBar 实时显示进度条；完成自动结算；链式解锁提示
- **双语指令**：中/英文子命令均可（`/任务` = `/quest`、claim = 领取、info = 信息、auto = 自动领取）
- **GUI 任务面板**：每日 / 进行中 / 主线三分区，章节两级导航，能力详情一目了然

## 命令

| 命令 | 说明 | 权限 |
|---|---|---|
| `/任务` 或 `/quest` | 打开任务面板 | quest.use |
| `/任务 领取 <id>`（claim） | 领取任务 | quest.use |
| `/任务 进度`（progress） | 查看进行中任务 | quest.use |
| `/任务 完成 <id>`（complete） | 手动提交 | quest.use |
| `/任务 自动领取 开\|关`（auto） | 进服自动领今日任务 | quest.use |
| `/任务 刷新`（refresh） | 刷新每日任务板 | quest.admin |
| `/任务 重置 <玩家>`（reset） | 清空玩家任务 | quest.admin |
| `/任务 重载`（reload） | 重载配置 | quest.admin |

权限：`quest.use` 所有人 / `quest.admin` 默认 OP。

## 配置示例

```yaml
# config.yml
settings:
  daily-count: 3          # 每天随机抽几个每日任务
  sidebar-enabled: true   # 侧边栏进度显示
  auto-complete: true     # 达成目标自动完成（false 需手动提交）

# quests.yml —— 主线任务链（五章 50 环，链式解锁）
chain:
  first_meal:
    name: "&b第一餐"
    type: kill
    mob: ZOMBIE
    amount: 10
    rewards:
      economy: { exp: 6 }                      # 装了 Economy 时
      fallback: { exp: 10 }                    # 没装 Economy 时
    next: grow_crops                           # 链式解锁

# daily-pool.yml —— 每日任务池（28 个，每天随机抽 daily-count 个）
daily-pool:
  dz_small:
    name: "&c清理僵尸"
    type: kill
    mob: ZOMBIE
    amount: 15
    rewards:
      economy: { money: 30, exp: 5 }
      fallback: { exp: 7 }
```

## 安装

1. 下载 `quest-1.1.0.jar` 放入服务器 `plugins/`
2. 重启服务器
3. 配置 `plugins/Quest/quests.yml`（主线）、`daily-pool.yml`（每日池）、`config.yml`（抽取数/反馈开关）

## 兼容性

- 支持核心：Spigot / Paper / Purpur / Leaves
- API 版本：1.16+（spigot-api 1.16.5 编译，理论兼容至最新）
- Java：17+
- 前置依赖：无（Economy 可选联动）

## 开源协议

MIT License

---

# Quest (English)

Config-driven quest plugin: kill / mine / reach objectives, daily quest pool (28 random-picked), 5-chapter main story chain (50 quests), dual-reward mode, bilingual commands. Zero dependencies.

## Features

- 3 objective types: kill mobs / mine blocks (mature crops only) / reach locations
- Daily pool of 28 quests, 3 randomly picked per day per player, resets daily; read-only pool viewer
- One-click claim all daily quests; `/任务 自动领取 开` auto-claims on join
- 5-chapter main story (50 quests) with chapter navigation and chain unlocking; smooth reward curve
- Balanced design: XP always, items as-needed, money rare, points reserved for chapter finals; no diamond gear / netherite armor rewards
- Dual reward mode: Economy plugin (gold+points+items) or fallback (1.5x vanilla XP + items)
- ActionBar live progress; auto-complete; bilingual commands (EN/CN)

## Compatibility

- Server: Spigot / Paper / Purpur / Leaves
- API version: 1.16+
- Java 17+
- Dependencies: none (optional Economy)

## License

MIT License

## Author

**TinyAII**