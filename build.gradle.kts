// Top-level build file where you can add configuration options common to all sub-projects/modules.
@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
}

// 确保所有项目使用 JDK 17
allprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "17"
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

// JVM 版本验证任务
tasks.register("verifyJvmVersion") {
    doLast {
        println("=== JVM Version Information ===")
        println("Java version: ${System.getProperty("java.version")}")
        println("Java vendor: ${System.getProperty("java.vendor")}")
        println("Java home: ${System.getProperty("java.home")}")
        println("JVM version: ${System.getProperty("java.vm.version")}")
        
        val javaVersion = JavaVersion.current()
        println("Detected Java version: $javaVersion")
        
        if (javaVersion != JavaVersion.VERSION_17) {
            throw GradleException("Project requires JDK 17, but found: $javaVersion")
        } else {
            println("✅ JDK 17 verified successfully!")
        }
    }
}