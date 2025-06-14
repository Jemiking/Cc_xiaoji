import com.ccxiaoji.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidHiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.google.devtools.ksp")
                apply("dagger.hilt.android.plugin")
            }

            dependencies {
                add("implementation", libs.findLibrary("hilt.android").get())
                add("ksp", libs.findLibrary("hilt.compiler").get())
                
                // WorkManager Hilt integration if needed
                if (name == "ledger") {
                    add("implementation", libs.findLibrary("hilt.work").get())
                    add("ksp", libs.findLibrary("androidx.hilt.compiler").get())
                }
            }
        }
    }
}