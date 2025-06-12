import com.android.build.gradle.LibraryExtension
import com.ccxiaoji.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply {
                apply("ccxiaoji.android.library")
                apply("ccxiaoji.android.library.compose")
                apply("ccxiaoji.android.hilt")
            }

            extensions.configure<LibraryExtension> {
                testOptions {
                    unitTests {
                        isIncludeAndroidResources = true
                    }
                }
            }

            dependencies {
                // Core modules - Feature模块的通用依赖
                add("implementation", project(":core:common"))
                add("implementation", project(":core:ui"))
                
                // Lifecycle - 基础的 runtime-ktx 仍需要保留
                add("implementation", libs.findLibrary("androidx.lifecycle.runtime.ktx").get())
                // viewmodel.compose 已由 library.compose 提供
                // add("implementation", libs.findLibrary("androidx.lifecycle.viewmodel.compose").get())
                
                // Navigation - 已由 library.compose 提供
                // add("implementation", libs.findLibrary("androidx.navigation.compose").get())
                // add("implementation", libs.findLibrary("hilt.navigation.compose").get())
                
                // Gson
                add("implementation", "com.google.code.gson:gson:2.10.1")
                
                // Testing
                add("testImplementation", libs.findLibrary("junit").get())
                add("testImplementation", libs.findLibrary("mockk").get())
                add("testImplementation", libs.findLibrary("kotlinx.coroutines.test").get())
                
                add("androidTestImplementation", libs.findLibrary("androidx.test.ext").get())
                add("androidTestImplementation", libs.findLibrary("androidx.espresso.core").get())
                // compose.ui.test.junit4 已由 library.compose 提供
                // add("androidTestImplementation", libs.findLibrary("compose.ui.test.junit4").get())
            }
        }
    }
}