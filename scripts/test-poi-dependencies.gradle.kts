// 测试POI依赖解析的临时脚本
configurations {
    create("testPoiRelocation") {
        isCanBeResolved = true
        isCanBeConsumed = false
    }
}

repositories {
    mavenCentral()
    google()
    maven { url = uri("https://maven.aliyun.com/repository/public") }
}

dependencies {
    add("testPoiRelocation", "org.apache.poi:poi-ooxml-lite:5.2.5") {
        exclude(group = "org.apache.logging.log4j")
        exclude(group = "commons-logging")
    }
    add("testPoiRelocation", "org.apache.poi:poi:5.2.5")
    add("testPoiRelocation", "org.apache.poi:poi-ooxml:5.2.5")
    add("testPoiRelocation", "org.apache.xmlbeans:xmlbeans:5.1.1")
    add("testPoiRelocation", "org.apache.commons:commons-collections4:4.4")
    add("testPoiRelocation", "org.apache.commons:commons-compress:1.21")
}

tasks.register("testPoiDependencies") {
    doLast {
        val config = configurations.getByName("testPoiRelocation")
        println("=== POI Dependency Resolution Test ===")
        try {
            val resolvedDeps = config.resolvedConfiguration.resolvedArtifacts
            println("✅ Successfully resolved ${resolvedDeps.size} artifacts:")
            resolvedDeps.forEach { artifact ->
                println("  - ${artifact.moduleVersion.id}")
            }
        } catch (e: Exception) {
            println("❌ Failed to resolve dependencies: ${e.message}")
            
            // 尝试逐个解析
            config.allDependencies.forEach { dep ->
                try {
                    println("Testing ${dep.group}:${dep.name}:${dep.version}")
                    val singleConfig = configurations.create("test_${dep.name}") {
                        isCanBeResolved = true
                        isCanBeConsumed = false
                    }
                    dependencies.add(singleConfig.name, "${dep.group}:${dep.name}:${dep.version}")
                    val resolved = singleConfig.resolvedConfiguration.resolvedArtifacts
                    println("  ✅ ${dep.group}:${dep.name}:${dep.version} - ${resolved.size} artifacts")
                } catch (ex: Exception) {
                    println("  ❌ ${dep.group}:${dep.name}:${dep.version} - ${ex.message}")
                }
            }
        }
    }
}

// 默认任务
defaultTasks("testPoiDependencies")