import java.io.File

/**
 * Safe Delete检查任务
 * 用于分析代码中的依赖关系，防止误删重要文件
 */
tasks.register("checkUnusedFiles") {
    description = "检查项目中未使用的Kotlin文件"
    group = "verification"
    
    doLast {
        val srcDirs = listOf(
            file("app/src/main/java"),
            file("app/src/main/kotlin"),
            file("core/common/src/main/kotlin"),
            file("core/ui/src/main/kotlin"),
            file("core/database/src/main/kotlin"),
            file("core/network/src/main/kotlin")
        )
        
        // 收集所有Kotlin文件
        val allKotlinFiles = mutableSetOf<File>()
        srcDirs.forEach { dir ->
            if (dir.exists()) {
                dir.walkTopDown()
                    .filter { it.extension == "kt" }
                    .forEach { allKotlinFiles.add(it) }
            }
        }
        
        // 分析import语句，构建依赖关系图
        val importMap = mutableMapOf<String, MutableSet<String>>()
        val classToFile = mutableMapOf<String, File>()
        
        allKotlinFiles.forEach { file ->
            val className = file.nameWithoutExtension
            classToFile[className] = file
            
            file.readLines().forEach { line ->
                if (line.trim().startsWith("import ")) {
                    val importedClass = line.substringAfter("import ")
                        .substringBefore(";")
                        .trim()
                        .substringAfterLast(".")
                    
                    if (!importedClass.startsWith("*")) {
                        importMap.getOrPut(importedClass) { mutableSetOf() }.add(file.path)
                    }
                }
            }
        }
        
        // 查找未被引用的文件
        val unusedFiles = mutableListOf<File>()
        classToFile.forEach { (className, file) ->
            if (!importMap.containsKey(className)) {
                // 检查是否是入口类（Activity、Application、@HiltAndroidApp等）
                val content = file.readText()
                val isEntryPoint = content.contains("@HiltAndroidApp") ||
                    content.contains("class MainActivity") ||
                    content.contains("@AndroidEntryPoint") ||
                    content.contains("@Module") ||
                    content.contains("@HiltViewModel") ||
                    file.path.contains("di/") // DI模块通常不被直接import
                
                if (!isEntryPoint) {
                    unusedFiles.add(file)
                }
            }
        }
        
        // 生成报告
        println("=== Unused Files Report ===")
        println("Total Kotlin files: ${allKotlinFiles.size}")
        println("Potentially unused files: ${unusedFiles.size}")
        println()
        
        if (unusedFiles.isNotEmpty()) {
            println("⚠️ The following files appear to be unused:")
            unusedFiles.forEach { file ->
                println("  - ${file.relativeTo(rootDir)}")
            }
            println()
            println("⚡ Before deleting these files:")
            println("  1. Use IDE's 'Find Usages' to double-check")
            println("  2. Check if they are referenced in XML layouts")
            println("  3. Check if they are used via reflection")
            println("  4. Consider if they are test files or examples")
        } else {
            println("✅ No obviously unused files found")
        }
        
        // 检查最近删除的文件是否还被引用
        println()
        println("=== Checking for missing dependencies ===")
        var missingCount = 0
        importMap.forEach { (className, usedInFiles) ->
            if (!classToFile.containsKey(className) && 
                !className.matches(Regex("^[A-Z_]+$")) && // 排除常量
                !className.startsWith("R") && // 排除R文件
                className != "BuildConfig") {
                
                println("❌ Missing: $className")
                println("   Used in:")
                usedInFiles.forEach { file ->
                    println("   - ${file.substringAfter(rootDir.path)}")
                }
                missingCount++
            }
        }
        
        if (missingCount > 0) {
            println()
            println("⚠️ Found $missingCount missing dependencies!")
            println("These classes are imported but their files don't exist.")
        } else {
            println("✅ No missing dependencies found")
        }
    }
}

/**
 * 在删除文件前进行安全检查
 */
tasks.register("safeDelete") {
    description = "安全删除文件，先检查依赖关系"
    group = "verification"
    
    doLast {
        val filesToDelete = project.properties["files"]?.toString()?.split(",") ?: emptyList()
        
        if (filesToDelete.isEmpty()) {
            println("Usage: ./gradlew safeDelete -Pfiles=path/to/file1.kt,path/to/file2.kt")
            return@doLast
        }
        
        println("=== Safe Delete Check ===")
        println("Files to delete:")
        filesToDelete.forEach { println("  - $it") }
        println()
        
        // 检查每个文件是否被其他文件引用
        filesToDelete.forEach { filePath ->
            val file = file(filePath)
            if (file.exists()) {
                val className = file.nameWithoutExtension
                println("Checking references for: $className")
                
                // 在所有源文件中搜索引用
                var foundReferences = false
                fileTree("app/src").matching {
                    include("**/*.kt")
                    include("**/*.java")
                    exclude(filePath)
                }.forEach { sourceFile ->
                    val content = sourceFile.readText()
                    if (content.contains("import.*$className".toRegex()) ||
                        content.contains("$className[\\s<(.]".toRegex())) {
                        if (!foundReferences) {
                            println("  ⚠️ Found references:")
                            foundReferences = true
                        }
                        println("    - ${sourceFile.relativeTo(rootDir)}")
                    }
                }
                
                if (!foundReferences) {
                    println("  ✅ No references found, safe to delete")
                } else {
                    println("  ❌ File is still being used!")
                }
                println()
            } else {
                println("  ❓ File not found: $filePath")
            }
        }
        
        println("⚡ To proceed with deletion, review the above results carefully")
    }
}