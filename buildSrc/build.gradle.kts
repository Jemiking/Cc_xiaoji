plugins {
    `kotlin-dsl`
}

repositories {
    // 直接使用官方源，通过代理访问
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("com.android.tools.build:gradle:8.2.1")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.21")
    implementation("com.google.dagger:hilt-android-gradle-plugin:2.48.1")
    implementation("com.google.devtools.ksp:symbol-processing-gradle-plugin:1.9.21-1.0.15")
    implementation("org.jetbrains.kotlin:kotlin-serialization:1.9.21")
}