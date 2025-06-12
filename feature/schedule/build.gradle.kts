plugins {
    id("ccxiaoji.android.feature")
}

android {
    namespace = "com.ccxiaoji.feature.schedule"

    sourceSets {
        getByName("main") {
            java.srcDirs(
                "api/src/main/kotlin",
                "data/src/main/kotlin",
                "domain/src/main/kotlin",
                "presentation/src/main/kotlin"
            )
        }
    }
}

dependencies {
    // Feature-specific dependency: database access
    implementation(project(":core:database"))
}