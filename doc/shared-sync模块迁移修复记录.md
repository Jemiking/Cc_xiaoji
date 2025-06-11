# shared-sync模块迁移修复记录

## 问题描述
编译错误：`Unresolved reference: ccxiaoji` 在 shared/sync/build.gradle.kts 文件中

## 根本原因
错误地使用了不存在的插件别名：
- `alias(libs.plugins.ccxiaoji.android.feature)`
- `alias(libs.plugins.ccxiaoji.android.library.compose)`

## 解决方案
参考shared/user模块的配置方式，使用正确的插件配置。

## 具体修改

### 1. 修改shared/sync/build.gradle.kts
```kotlin
// 修改前
plugins {
    alias(libs.plugins.ccxiaoji.android.feature)
    alias(libs.plugins.ccxiaoji.android.library.compose)
}

// 修改后
plugins {
    id("ccxiaoji.android.library")
    id("ccxiaoji.android.hilt")
}
```

### 2. 添加sourceSets配置
```kotlin
android {
    namespace = "com.ccxiaoji.shared.sync"

    sourceSets {
        getByName("main") {
            java.srcDirs(
                "api/src/main/kotlin",
                "data/src/main/kotlin",
                "domain/src/main/kotlin"
            )
        }
    }
}
```

### 3. 更新依赖配置
- 移除对子模块的依赖（:shared:sync:api等）
- 添加必要的核心模块依赖
- 调整shared模块依赖为`:shared:user`而不是`:shared:user:api`

### 4. 清理settings.gradle.kts
移除不再需要的子模块引用：
```kotlin
// 删除了
include(":shared:sync:api")
include(":shared:sync:data")
include(":shared:sync:domain")
```

### 5. 删除子模块build文件
删除了不再需要的子模块build.gradle.kts文件

## 模块结构说明
shared/sync模块现在采用与shared/user相同的结构：
- 使用单一模块配置
- 通过sourceSets包含api/data/domain源代码
- 不再使用子模块方式

这种方式与项目中其他shared模块保持一致，简化了依赖管理。