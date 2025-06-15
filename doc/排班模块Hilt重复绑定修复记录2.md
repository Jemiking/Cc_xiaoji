# 排班模块Hilt重复绑定修复记录（第二次）

## 修复时间
2025-06-13

## 问题描述
编译失败，Hilt报告`ScheduleApi`被重复绑定：
- `app模块`：ScheduleBridgeModuleImpl绑定了app.bridge.schedule.ScheduleApiImpl
- `feature:schedule模块`：ScheduleModule绑定了feature.schedule.data.ScheduleApiImpl

## 根本原因
在集成阶段错误地在app模块创建了ScheduleApiImpl，但feature:schedule模块已经包含了完整的API实现。这违背了模块化架构原则。

## 修复方案
采用方案一：删除app模块的重复实现，使用feature模块的实现

## 具体操作
1. 删除了整个`/app/src/.../bridge/schedule/`目录
2. 删除了`/app/src/.../di/ScheduleBridgeModuleImpl.kt`文件

## 正确的架构理解
### 模块职责划分：
- **feature模块**：
  - 包含完整的api/data/domain/presentation层
  - API实现应在feature模块的data层
  - 通过依赖注入暴露API实现

- **app模块**：
  - 只负责导航器实现（因为需要NavController）
  - 模块集成和组装
  - 不应包含业务逻辑实现

### 依赖注入结构：
```
feature:schedule模块
└── ScheduleModule
    ├── @Binds ScheduleApi (由ScheduleApiImpl实现)
    └── @Binds ScheduleNavigator (由app模块提供)

app模块
└── NavigationModule
    └── @Binds ScheduleNavigator (由ScheduleNavigatorImpl实现)
```

## 经验教训
1. **理解模块边界**：API实现属于feature模块，不应在app模块重复实现
2. **遵循单一职责**：app模块只负责组装，不实现业务逻辑
3. **检查现有实现**：创建新文件前先检查feature模块是否已有实现
4. **架构一致性**：所有feature模块应遵循相同的结构模式

## 编译验证
```bash
./gradlew clean
./gradlew :feature:schedule:compileDebugKotlin
./gradlew :app:compileDebugKotlin
./gradlew :app:hiltJavaCompileDebug
```

## 后续建议
1. 检查其他模块是否有类似的重复实现
2. 在CLAUDE.md中明确模块职责划分
3. 创建模块开发检查清单，避免类似错误