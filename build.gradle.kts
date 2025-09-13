// Top-level build file where you can add configuration options common to all sub-projects/modules.
// Top-level build file configuration
// 仓库配置已移至 settings.gradle.kts 中集中管理

import java.util.regex.Pattern

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

// 轻量级重复枚举名校验（ledger 模块）：
// 约束：与数据库持久化一一对应的枚举只允许在 data/local/entity 定义；
// 若 domain 再定义同名枚举，容易产生语义/映射分歧，需阻断。
val verifyDuplicateEnumsLedger = tasks.register("verifyDuplicateEnumsLedger") {
    group = "verification"
    description = "检查 feature/ledger 的 data/local/entity 与 domain 目录下是否存在重名枚举（技术债防护）"

    doLast {
        fun collectEnumNames(dir: File): Set<String> {
            if (!dir.exists()) return emptySet()
            val regex = Pattern.compile("\\benum\\s+class\\s+([A-Za-z0-9_]+)")
            val names = mutableSetOf<String>()
            dir.walkTopDown()
                .filter { it.isFile && it.extension == "kt" }
                .forEach { file ->
                    val text = file.readText()
                    val m = regex.matcher(text)
                    while (m.find()) {
                        names += m.group(1)
                    }
                }
            return names
        }

        val dataDir = file("feature/ledger/src/main/kotlin/com/ccxiaoji/feature/ledger/data/local/entity")
        val domainDir = file("feature/ledger/src/main/kotlin/com/ccxiaoji/feature/ledger/domain")

        val dataEnums = collectEnumNames(dataDir)
        val domainEnums = collectEnumNames(domainDir)

        // 允许的重名白名单（如确属同名不同语义且明确不落库的 UI 枚举，可在此登记）
        val allowlist = setOf<String>()

        val duplicates = (dataEnums intersect domainEnums).filterNot { allowlist.contains(it) }
        if (duplicates.isNotEmpty()) {
            throw GradleException(
                "检测到重复的枚举名（data/local/entity 与 domain 同名）：${duplicates.joinToString()}\n" +
                "请保留持久化相关的唯一定义于 data/local/entity，领域层若需不同语义请更名（例如后缀加 Domain/Ui）。"
            )
        }
    }
}

// 将校验挂到所有子模块的 check 阶段
subprojects {
    tasks.matching { it.name == "check" }.configureEach {
        dependsOn(verifyDuplicateEnumsLedger)
    }
}
