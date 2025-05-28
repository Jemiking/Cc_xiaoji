# Cc 小记 — 全量设计与交付基线 (v0.9)

> 本文件补齐开发所需的全部设计缺口，作为 **MVP→1.0** 的唯一权威来源。后续如需修改，须走 CR 流程并版本化 (`v0.9.x`).

---

## 目录

1. 产品需求文档 (PRD)
2. 用户故事 & 验收标准
3. 数据库 ERD
4. API 合同 (OpenAPI 摘要)
5. 同步/冲突协议
6. UI / UX 指南
7. 非功能需求 (NFR)
8. 测试计划 & 质量门槛

---

## 1. 产品需求文档 (PRD)

### 1.1 目标与愿景

* **一站式个人效率中枢**：整合记账、代办、习惯、倒数日四大功能，以“首页卡片 + 底栏模块”呈现。
* **离线优先 & 秒级同步**：无网可用，联网 ≤3 s 完成双向同步。
* **插件式可扩展**：未来新增模块（如日记、目标管理）可 3 天内接入。

### 1.2 目标用户画像

| Persona       | 场景          | 痛点       | 关键需求          |
| ------------- | ----------- | -------- | ------------- |
| 小白领「小柯」(25)   | 日常收支 & 工作任务 | 多 App 切换 | 一键记账 + 今天代办汇总 |
| 研究生「阿楠」(23)   | 论文计划 & 倒数毕业 | 时间碎片化    | 离线记录 + 倒数日提醒  |
| 斜杠妈妈「Mia」(32) | 家庭账本 & 习惯养成 | 多端同步     | 家庭共享账本 & 打卡激励 |

### 1.3 MVP 范围

* **记账**：新增/编辑/删除、分类、月度报告
* **代办**：CRUD、提醒、优先级
* **习惯**：创建习惯、每日打卡、连续天数统计
* **倒数日**：创建/编辑倒数事件、桌面小组件 (Android 12+)

### 1.4 里程碑

| 里程碑          | 时间      | 出口标准            |
| ------------ | ------- | --------------- |
| M0 PRD 冻结    | Week 2  | 本文件签字锁定         |
| M1 Framework | Week 6  | 登录+同步 SDK Green |
| M2 MVP Alpha | Week 10 | 4 模块核心功能可用      |
| M3 Beta      | Week 14 | Crash ≤1%, 功能冻结 |
| M4 GA 1.0    | Week 16 | 商店公测上架          |

---

## 2. 用户故事 & 验收标准 (摘选)

| #      | As a | I want   | So that | Acceptance Criteria                                                                                 |
| ------ | ---- | -------- | ------- | --------------------------------------------------------------------------------------------------- |
| US‑L01 | 用户   | 记录一笔支出   | 管理日常花销  | *Given* 打开「快速记账」, *When* 输入金额+类别+备注并点击保存, *Then* 新记录出现在今日列表,<br/>金额显示正确；无网时保存在本地并标为 `pending_sync`. |
| US‑T02 | 用户   | 设置任务提醒   | 不错过截止   | *Given* 创建任务并选择日期时间, *When* 到点, *Then* 收到前台通知且通知点击直达任务详情.                                           |
| US‑H05 | 用户   | 查看连续打卡天数 | 维持习惯动力  | 打开习惯卡片显示当前「🔥 连续 X 天」计数; 若漏打则清零.                                                                    |
| US‑C03 | 用户   | 添加倒数日小组件 | 随时查看天数  | 长按桌面选择「Cc 倒数」小组件，选择事件后在桌面展示 D‑Day.                                                                  |

*完整表格请见 `docs/user_stories.xlsx` (待导入 Jira)*

---

## 3. 数据库 ERD  (SQLLite / PostgreSQL 同构)

```text
[users]──1——＊[accounts]
   │               │
   │               └──＊[transactions]
   │
   ├──＊[tasks]──＊[task_tags]
   │
   ├──＊[habits]──＊[habit_records]
   │
   └──＊[countdowns]
```

### 表结构 (关键字段)

* **users** `(id UUID PK, email TEXT UNIQUE, created_at INT)`
* **transactions** `(id UUID PK, user_id FK, amount_cents INT, category TEXT, created_at INT, note TEXT, updated_at INT)`
* **tasks** `(id UUID PK, user_id FK, title TEXT, due_at INT, priority INT, completed INT, updated_at INT)`
* **habits** `(id UUID PK, user_id FK, title TEXT, period TEXT, target INT)`
* **habit\_records** `(id UUID PK, habit_id FK, record_date INT)`
* **countdowns** `(id UUID PK, user_id FK, title TEXT, target_date INT, emoji TEXT)`

> **同步字段**：所有业务表必须有 `updated_at` (epoch ms)；删除使用 `is_deleted` 软删。

---

## 4. API 合同 (OpenAPI 3.1 概要)

### 4.1 安全

* OAuth2 Password / Refresh
* JWT 放 Header `Authorization: Bearer <token>`

### 4.2 示例端点

```yaml
POST /v1/transactions
  requestBody:
    amount_cents: integer
    category: string
    note: string
    created_at: integer (client-generated)
  responses:
    201: { id: UUID, server_time: integer }
    400: INVALID_PARAMS
```

```yaml
GET /v1/sync?since={timestamp}
  → 200: { changes: [ { table: string, rows: [...] } ] }
```

*完整 OpenAPI YAML 见 `api/openapi.yaml`*  (已有占位，待补完属性约 60%)

---

## 5. 同步 / 冲突协议

1. **变更捕获**：客户端对每次 CRUD 写入 `change_log(id, table, row_id, op, payload, ts)`。
2. **上传阶段**：按 `ts` 升序批量 POST `/v1/sync/upload`。
3. **合并策略**：服务器字段级 Last‑Write‑Wins；如金额与备注同时冲突则合并为两条 history。
4. **拉取阶段**：客户端携 `last_sync` 调 `/v1/sync?since=` 获取增量。
5. **离线冲突提示**：UI 若检测本地修改被覆盖，展示“已同步更新”横幅并提供「恢复本地草稿」入口。
6. **失败重试**：指数退避 1→2→4→8→… 最长 1h；超过 24h 标为 `sync_error` 并告警。

---

## 6. UI / UX 指南

### 6.1 信息架构

```
BottomNav: Home | Ledger | Todo | Habit | Profile
Home = 卡片聚合 (可自定义顺序)
```

### 6.2 设计 Token

| Token           | 值         | 说明   |
| --------------- | --------- | ---- |
| `color.primary` | #3A7AFE   | 品牌蓝  |
| `radius.lg`     | 24dp      | 卡片圆角 |
| `font.h1`       | 24sp Bold |      |

### 6.3 可访问性 & 国际化

* 动态字体适配；暗色模式默认跟随系统
* 全组件提供 `contentDescription`
* 文案文件 `strings.xml` 中英双语；后续支持 i18n via Crowdin

### 6.4 关键流程原型

* Figma URL: `https://figma.com/file/.../CcXiaoJi-v09` (已分享编辑权限)

---

## 7. 非功能需求 (NFR)

| 类别 | 指标                                     |
| -- | -------------------------------------- |
| 性能 | 冷启动 ≤1.5 s；帧丢失率 < 1% (90fps)           |
| 稳定 | CrashFree ≥ 99.5%；ANR ≤ 0.1%           |
| 安全 | AES‑256 本地加密；OWASP Mobile Top‑10 全通过   |
| 隐私 | GDPR + 中国《个人信息保护法》双合规；数据最小化原则          |
| 监控 | Prometheus + Grafana；关键接口 P95 < 300 ms |

---

## 8. 测试计划 & 质量门槛

### 8.1 测试矩阵

| 维度  | 取值                         |
| --- | -------------------------- |
| OS  | Android 8 / 10 / 13        |
| CPU | 32‑bit, 64‑bit             |
| 网络  | 4G (200 ms RTT), Wi‑Fi, 离线 |
| 语言  | zh‑CN, en‑US               |

### 8.2 覆盖率目标

* 单元测试 ≥ 70%
* Compose UI Test 覆盖主要流程（Ledger、Todo）
* E2E：Detox on Device Farm

### 8.3 自动化脚本

* `./gradlew testDebugUnitTest`
* `./gradlew connectedCheck` (Firebase TestLab)
* `./gradlew detekt ktlintFormat`  确保零警告

---

## 结尾

此文档一经甲方-A 确认，视为「开发唯一源」。后续需求变更请提交 CR → 评审 → 更新版本号。

> **下一步**：
>
> * 甲方-A 审批 (`v0.9 → v1.0`)。
> * 技术团队按文档启动 Sprint 1 (框架基线)。
