# Schedule 模块组件分层规则

> **创建日期**: 2025-12-24
> **版本**: v1.0
> **关联文档**: [排班助手UI重构完整方案.md](./排班助手UI重构完整方案.md)

---

## 1. 概述

本文档定义了 Schedule 模块 UI 组件的分层规则，明确 `feature/schedule/presentation/uikit/` 与 `core/ui/` 之间的边界和上移条件。

### 1.1 设计原则

```
┌─────────────────────────────────────────────────────────────┐
│  两阶段策略：先在模块内验证，成熟后再上移到 core/ui        │
│  避免过早抽象：组件 API 稳定后再考虑通用化                  │
│  业务无关性：上移组件必须不含任何业务语义                   │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. 组件层级定义

### 2.1 层级架构图

```
┌─────────────────────────────────────────────────────────────┐
│                        app 模块                              │
│  (应用壳层，仅做导航编排)                                    │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                   feature/schedule 模块                      │
│  ┌─────────────────────────────────────────────────────────┐│
│  │ presentation/screen/     ← 页面级组合                   ││
│  │   CalendarScreen, ShiftManageScreen, ...                ││
│  └─────────────────────────────────────────────────────────┘│
│  ┌─────────────────────────────────────────────────────────┐│
│  │ presentation/uikit/      ← Schedule UI Kit (本模块专用) ││
│  │   ScheduleScaffold, ScheduleDayCell, ShiftPill, ...     ││
│  └─────────────────────────────────────────────────────────┘│
│  ┌─────────────────────────────────────────────────────────┐│
│  │ presentation/theme/      ← 模块主题和设计规范           ││
│  │   ScheduleDesignSpecs.kt, ThemeManager.kt               ││
│  └─────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                       core/ui 模块                           │
│  ┌─────────────────────────────────────────────────────────┐│
│  │ components/              ← 通用 UI 组件                 ││
│  │   ModernCard, FlatButton, FlatFAB, ...                  ││
│  └─────────────────────────────────────────────────────────┘│
│  ┌─────────────────────────────────────────────────────────┐│
│  │ theme/                   ← 全局设计系统                 ││
│  │   DesignTokens, Color, Theme, Type, ...                 ││
│  └─────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────┘
```

### 2.2 各层职责

| 层级 | 位置 | 职责 | 示例 |
|------|------|------|------|
| **Screen** | `presentation/screen/` | 页面级组合，使用 uikit 组件 | CalendarScreen |
| **UI Kit** | `presentation/uikit/` | Schedule 模块专用组件 | ScheduleDayCell, ShiftPill |
| **Theme** | `presentation/theme/` | 模块设计规范 | ScheduleDesignSpecs |
| **Core UI** | `core/ui/components/` | 跨模块通用组件 | ModernCard, FlatButton |
| **Core Theme** | `core/ui/theme/` | 全局设计令牌 | DesignTokens |

---

## 3. 组件分类标准

### 3.1 Schedule UI Kit 组件（模块内部）

**特征**:
- 包含 Schedule 业务语义（班次、排班、日历等）
- 依赖 Schedule 领域模型（Shift, Schedule 等）
- 仅在 Schedule 模块内使用

**组件清单**:

| 组件 | 业务语义 | 备注 |
|------|----------|------|
| `ScheduleScaffold` | 排班页面脚手架 | 含排班相关 TopAppBar/FAB |
| `ScheduleTopAppBar` | 排班顶栏 | 含月份选择器 |
| `ScheduleDayCell` | 日历日格子 | 显示排班信息 |
| `ScheduleCalendarGrid` | 月视图网格 | 排班日历核心 |
| `ShiftColorIndicator` | 班次颜色指示器 | 依赖 Shift 模型 |
| `ShiftPill` | 班次胶囊标签 | 依赖 Shift 模型 |
| `ShiftRow` | 班次列表行 | 依赖 Shift 模型 |
| `ShiftDock` | 班次浮动坞 | 快捷操作面板 |
| `ScheduleDragController` | 拖拽控制器 | 排班拖拽逻辑 |

### 3.2 core/ui 候选组件

**特征**:
- 无业务语义，纯 UI 逻辑
- 可被 ≥2 个模块复用
- API 稳定 ≥2 周

**候选清单**:

| 组件 | 通用性 | 当前状态 |
|------|--------|----------|
| `ScheduleSelectableRow` | 单选/多选行，无业务语义 | 待验证 |
| `LabeledValueCard` | 标签-值卡片，纯展示 | 待验证 |

---

## 4. 组件上移规则

### 4.1 上移门槛（必须全部满足）

```
┌─────────────────────────────────────────────────────────────┐
│  ✅ 条件1: 被 ≥2 个模块引用                                 │
│  ✅ 条件2: 不含任何业务语义（无 Shift/Schedule 等类型）     │
│  ✅ 条件3: API 稳定 ≥2 周（无破坏性变更）                   │
│  ✅ 条件4: 通过 Code Review 审查                            │
└─────────────────────────────────────────────────────────────┘
```

### 4.2 上移流程

```
1. 识别候选
   │  组件在 schedule 模块内稳定运行
   │  发现其他模块有相似需求
   ▼
2. 评估可行性
   │  检查是否满足4个上移条件
   │  评估 API 设计是否足够通用
   ▼
3. 提取抽象
   │  移除所有业务语义依赖
   │  泛化参数类型（使用泛型或接口）
   ▼
4. 迁移实施
   │  在 core/ui 创建新组件
   │  在 schedule 模块创建薄封装适配
   │  更新所有引用点
   ▼
5. 标记废弃
   │  原组件标记 @Deprecated
   │  设置移除 deadline（2周后）
   ▼
6. 清理旧代码
      移除废弃组件
```

### 4.3 上移示例

**Before (schedule 模块内)**:
```kotlin
// feature/schedule/presentation/uikit/ScheduleSelectableRow.kt
@Composable
fun ScheduleSelectableRow(
    shift: Shift?,  // ❌ 业务类型
    selected: Boolean,
    onClick: () -> Unit
) { ... }
```

**After (core/ui)**:
```kotlin
// core/ui/components/SelectableRow.kt
@Composable
fun <T> SelectableRow(
    item: T?,  // ✅ 泛型
    selected: Boolean,
    onClick: () -> Unit,
    leadingContent: @Composable (T?) -> Unit,
    trailingContent: @Composable RowScope.() -> Unit = {}
) { ... }

// feature/schedule/presentation/uikit/ShiftRow.kt (薄封装)
@Composable
fun ShiftRow(shift: Shift?, selected: Boolean, onClick: () -> Unit) {
    SelectableRow(
        item = shift,
        selected = selected,
        onClick = onClick,
        leadingContent = { s -> ShiftColorIndicator(s?.color) }
    )
}
```

---

## 5. 目录结构规范

### 5.1 Schedule UI Kit 目录

```
feature/schedule/src/main/kotlin/com/ccxiaoji/feature/schedule/presentation/
├── uikit/                          # Schedule UI Kit 组件库
│   ├── ScheduleDesignSpecs.kt      # 设计规范（颜色、尺寸）
│   ├── scaffold/
│   │   ├── ScheduleScaffold.kt
│   │   └── ScheduleTopAppBar.kt
│   ├── calendar/
│   │   ├── ScheduleDayCell.kt
│   │   ├── ScheduleCalendarGrid.kt
│   │   └── ScheduleDragController.kt
│   ├── shift/
│   │   ├── ShiftColorIndicator.kt
│   │   ├── ShiftPill.kt
│   │   ├── ShiftRow.kt
│   │   └── ShiftDock.kt
│   └── common/
│       ├── ScheduleCard.kt
│       ├── ScheduleSectionCard.kt
│       └── ScheduleSelectableRow.kt
│
├── preview/                        # 组件预览 Catalog
│   ├── ScheduleComponentCatalog.kt
│   └── previews/
│       ├── ShiftComponentPreviews.kt
│       └── CalendarComponentPreviews.kt
│
└── theme/                          # 主题管理
    ├── ThemeManager.kt             # 已有
    └── (ScheduleDesignSpecs.kt)    # 可选：也可放在 uikit/
```

### 5.2 命名规范

| 类型 | 命名规则 | 示例 |
|------|----------|------|
| 组件文件 | `Schedule{功能}.kt` 或 `Shift{功能}.kt` | ScheduleDayCell.kt |
| Preview 文件 | `{组件类别}Previews.kt` | ShiftComponentPreviews.kt |
| 设计规范 | `ScheduleDesignSpecs.kt` | - |

---

## 6. 依赖规则

### 6.1 允许的依赖方向

```
✅ schedule/screen → schedule/uikit → core/ui
✅ schedule/uikit → schedule/theme
✅ schedule/uikit → core/ui/theme (DesignTokens)
✅ schedule/uikit → schedule/domain/model (Shift, Schedule)
```

### 6.2 禁止的依赖方向

```
❌ core/ui → schedule (反向依赖)
❌ schedule/uikit → schedule/screen (反向依赖)
❌ schedule/uikit → schedule/data (跨层依赖)
❌ schedule/uikit → 其他 feature 模块 (横向依赖)
```

### 6.3 依赖检查方法

```kotlin
// 在 build.gradle.kts 中配置依赖检查
// 使用 Gradle 的 dependency constraints 或 Linter 规则
```

---

## 7. 审查清单

### 7.1 新增组件审查

- [ ] 组件放置在正确的目录层级
- [ ] 命名符合规范（Schedule/Shift 前缀）
- [ ] 不包含 core/ui 已有的重复功能
- [ ] 有对应的 @Preview 函数
- [ ] 参数设计合理，遵循 Compose 惯例

### 7.2 上移组件审查

- [ ] 满足4个上移条件
- [ ] 移除所有业务类型依赖
- [ ] 原组件标记 @Deprecated
- [ ] 更新所有引用点
- [ ] 在 core/ui 添加文档注释

---

## 附录 A: 现有组件映射

| 旧组件位置 | 新组件位置 | 状态 |
|------------|------------|------|
| `calendar/components/CalendarDayCell.kt` | `uikit/calendar/ScheduleDayCell.kt` | 待迁移 |
| `shift/components/ShiftCard.kt` | `uikit/shift/ShiftRow.kt` | 待迁移 |
| `schedule/components/ShiftSelectCard.kt` | `uikit/shift/ShiftRow.kt` | 待合并 |

---

## 附录 B: 变更记录

| 版本 | 日期 | 变更内容 |
|------|------|----------|
| v1.0 | 2025-12-24 | 初始版本 |

---

*本文档作为 Schedule 模块 UI 重构的组件治理规范，所有组件开发和迁移工作应遵循本规则。*
