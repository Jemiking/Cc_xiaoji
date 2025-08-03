// Top-level build file where you can add configuration options common to all sub-projects/modules.
// Top-level build file configuration
// 仓库配置已移至 settings.gradle.kts 中集中管理

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}


