# 自动记账 · 多通道捕获 UI 布局设计（通知监听 / 无障碍 并行｜当前：全自动软关闭）

> 目标：在“自动记账设置”中并行呈现两种捕获方式：通知栏监听（轻量，默认开）与无障碍捕获（重量，实验功能，默认关）。当前仅提供“半自动”体验，全自动为“软关闭”，后续按需求再开放。

---

## 1. 页面结构与导航

- 入口：记账设置 → 自动记账设置（已有）
  - 新增分组：支付结果捕获方式
    - 卡片一：通知栏监听（默认开启）
    - 卡片二：支付结果页辅助识别（实验，无障碍，默认关闭）
  - 二级导航：权限与引导（无障碍授权步骤）
  - 二级导航：诊断与统计（小卡片嵌入，点击可跳转调试面板）

```
AutoLedgerSettingsScreen
 ├─ 卡片：启用自动记账（总开关）
 ├─ 卡片：当前模式（只读：半自动；说明“全自动软关闭”）
 ├─ 分组：支付结果捕获方式
 │   ├─ 卡片：通知栏监听（默认开启）
 │   │   ├─ 开关：启用通知监听
 │   │   ├─ 状态：已连接/未连接（支持“重连/去授权”）
 │   │   └─ 文案：轻量方案，依赖系统通知；推荐默认开启
 │   └─ 卡片：支付结果页辅助识别（实验）
 │       ├─ 开关：启用无障碍（默认关）
 │       ├─ 状态：未授权/已授权（系统无障碍）
 │       ├─ 文案：仅在支付结果页读取必要可见文本，不采集/上传
 │       ├─ 操作：去授权（跳系统无障碍设置）
 │       └─ 操作：权限与引导（进入引导页）
 └─ 卡片：监听配置与诊断（迷你统计 + 查看调试）
```

- 引导页：无障碍授权与说明（新）
  - 分步卡片：
    1) 开启“微信支付页辅助识别（实验）”开关
    2) 系统设置 → 启用无障碍服务
    3) 回到微信，完成一笔“发红包/转账”，即可在结果页收到提示
  - 常见问题（可折叠）：权限未授权/机型限制/不在支付页/如何回退
  - 按钮：去系统授权、问题排查、自检完成

---

## 2. 组件与布局（Compose 草图）

### 2.1 捕获方式分组（两卡片）

- 组件：`Card + ListItem + Switch + Row(Buttons)`
- 文案建议：
  - 卡片一（通知栏监听）：
    - 标题：通知栏监听
    - 说明：轻量方案。依赖系统通知，推荐默认开启；当前仅支持半自动（全自动软关闭）。
    - 状态：监听服务已连接/未连接；重连/去授权
  - 卡片二（无障碍，实验）：
    - 标题：支付结果页辅助识别（实验）
    - 说明：重量方案。仅在支付结果页读取必要可见文本用于记账预填，不采集/上传；默认关闭，可随时关闭；当前仅支持半自动（全自动软关闭）。
    - 状态：无障碍已授权/未授权；去授权/引导
- 操作：
  - `Switch`：启用/禁用
  - `TextButton`：去授权（跳系统无障碍设置）
  - `TextButton`：权限与引导（进入引导页）

布局示意：
```
Card（通知栏监听）
 ├─ ListItem(headline: "通知栏监听", supporting: "依赖系统通知；推荐默认开启")
 ├─ Row(trailing: Switch(checked = ui.captureNlEnabled))
 └─ Row(align end) { TextButton("重连"); TextButton("去授权") }

Card（无障碍，实验）
 ├─ ListItem(headline: "支付结果页辅助识别（实验）", supporting: "仅在支付结果页读取必要可见文本…")
 ├─ Row(trailing: Switch(checked = ui.captureA11yEnabled))
 └─ Row(align end) { TextButton("权限与引导"); TextButton("去授权") }
```

### 2.2 无障碍引导页（新）

- 组件：`Scaffold + TopAppBar + Column + Step Cards + Buttons`
- 步骤卡片：
  1) 开启实验开关（应用内）
  2) 系统授权（跳转：无障碍设置页）
  3) 回到微信做一笔“发红包/转账”测试
- 常见问题（可折叠）：
  - 没反应：检查是否在支付完成页/金额是否显示/是否快速返回
  - 如何关闭：设置页开关/一键回退
  - 合规提示：不采集/不上传，仅本地解析

布局示意：
```
TopAppBar("微信辅助识别 · 引导")
Column(spaced)
 ├─ StepCard(1, 说明 + 状态)
 ├─ StepCard(2, 说明 + 按钮"去授权")
 ├─ StepCard(3, 说明 + 提示"建议先小额测试")
 ├─ FAQ(折叠)
 └─ Row(align end)
     ├─ TextButton("问题排查")
     └─ Button("自检完成")
```

### 2.3 监听诊断迷你卡片（设置页内嵌） + 一键自检

- 组件：`Card + Row(StatisticItem)`
- 字段：捕获数/跳过数/平均解析时延/误触发撤销率（仅Debug或开发者打开时显示）
- 操作：
  - `TextButton("查看调试记录")` → 跳转现有“自动记账调试面板”
  - `TextButton("自检")` → 一键自检（检查通知监听连接状态，提醒渠道设置，提供重连/去授权直达）

---

## 3. 状态与ViewModel（草案）

- UI State 扩展：
```kotlin
data class AutoLedgerSettingsUiState(
  val captureNlEnabled: Boolean = true,
  val captureA11yEnabled: Boolean = false,
  val a11yWechatEnabled: Boolean = false,
  val a11yGranted: Boolean = false,
  val a11yDebounceMs: Int = 300,
  val a11yWindowDedupSec: Int = 5,
  val a11yKillSwitch: Boolean = false,
  val a11yLastTriggerAt: Long? = null,
  val diagnostics: ListenerDiagnostics? = null // 复用/扩展
)
```

- 交互方法（示例）：
```kotlin
fun toggleCaptureNlEnabled(enabled: Boolean)
fun toggleCaptureA11yEnabled(enabled: Boolean)
fun toggleA11yWechatEnabled(enabled: Boolean) // 可细分到App级
fun openSystemAccessibilitySettings()
fun openPermissionGuide()
fun updateA11yDebounceMs(v: Int)
fun updateA11yWindowDedupSec(v: Int)
```

- 数据来源：
- DataStore：
  - `capture_nl_enabled`, `capture_a11y_enabled`
  - `a11y_wechat_enabled`, `a11y_debounce_ms`, `a11y_window_dedup_sec`, `a11y_kill_switch`
  - 无障碍授权检测：`Settings.Secure.getString("enabled_accessibility_services")` 中包含本应用服务

---

## 4. 字符串资源（建议）

- keys（示例）：
```
"capture_group_title" → "支付结果捕获方式"
"nl_title" → "通知栏监听"
"nl_desc" → "轻量方案。依赖系统通知，推荐默认开启；当前仅支持半自动（全自动软关闭）。"
"a11y_wechat_title" → "支付结果页辅助识别（实验）"
"a11y_wechat_desc" → "仅在微信支付结果页读取必要可见文本用于记账预填，不采集/上传。默认关闭，可随时关闭。"
"a11y_status_granted" → "无障碍：已授权"
"a11y_status_denied" → "无障碍：未授权（请前往系统设置开启）"
"a11y_go_grant" → "去授权"
"a11y_go_guide" → "权限与引导"
"a11y_guide_title" → "微信辅助识别 · 引导"
"a11y_step1" → "开启应用内的\"微信支付页辅助识别（实验）\"开关"
"a11y_step2" → "前往系统\"无障碍\"设置，启用本应用的服务"
"a11y_step3" → "回到微信完成一笔\"发红包/转账\"进行验证（建议小额）"
"a11y_faq_title" → "常见问题"
"a11y_troubleshoot" → "问题排查"
"a11y_selfcheck_done" → "自检完成"
```

---

## 5. 交互与状态流转

1) 默认：开关关闭 → 所有高级项隐藏；状态显示“未授权（灰色）”
2) 开启开关：若未授权 → 引导文案高亮，显示“去授权”
3) 完成授权：状态更新“已授权（绿色）”；提示进行一次小额验证
4) 命中后：设置页诊断卡显示“最近触发时间”等统计（仅提示，不展示敏感细节）
5) 一键回退：关掉开关即可停止所有解析；同时可由Kill-Switch 熔断

---

## 6. 可视化与可用性

- 调色：遵循 Material3；实验开关使用强调色搭配“实验”徽标
- 无障碍：所有关键按钮有 contentDescription；说明文本可由 TalkBack 朗读
- 小屏适配：卡片分块、滚动容器；按钮区贴底留白

---

## 7. 开发拆分与DoD（UI先行）

- M-UI-1：设置页新增“微信支付页辅助识别（实验）”卡片（含开关/说明/去授权/引导）
- M-UI-2：新增“无障碍引导页”（三步）
- M-UI-3：诊断迷你卡（统计占位+跳调试）
- M-UI-4：字符串与可本地化；深/浅色适配；TalkBack校验

DoD：
- Release 构建默认开关关闭；说明清晰；引导可用；所有按钮有效并可达对应设置页
- 不接入后端逻辑也不崩溃；状态占位显示正常

---

## 8. 风险与回退（UI阶段）

- 风险：用户对“实验”认知不足 → 强化说明文案与二次确认
- 回退：仅UI层，无障碍逻辑未接入时不影响主流程；可随时移除卡片

---

## 9. 附：低保真线框（ASCII）

```
[自动记账设置]
 ┌──────────────────────────────────────────────┐
 │ 支付结果捕获方式                               │
 │ ┌──────── 通知栏监听 ────────┐  [  开  ]      │
 │ │ 依赖系统通知；推荐默认开启  │[重连] [授权]  │
 │ └────────────────────────────┘              │
 │ ┌──── 支付结果页辅助识别（实验） ───┐ [  关  ] │
 │ │ 仅在支付结果页读取必要可见文本…  │[引导][授权]│
 │ └──────────────────────────────────┘        │
 └──────────────────────────────────────────────┘

[引导页]
 ┌──────────────────────────────────────────────┐
 │ 微信辅助识别 · 引导                           │
 │ 1. 开启应用内开关                             │
 │ 2. 去系统无障碍设置授权      [去授权]          │
 │ 3. 回微信做一笔发红包/转账                     │
 │ 常见问题(可展开)：…                           │
 │ [问题排查]                          [自检完成] │
 └──────────────────────────────────────────────┘
```
