package job

import com.android.build.gradle.AppExtension
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import utils.isRunAssembleTask
import utils.replaceFirstChar
import java.io.File
import java.util.Locale

/**
 * @author seven
 * @date 2024/8/28
 * @desc
 * 清除一些第三方使用的 transform ，导致jar 重复问题,譬如 alibaba Arouter
 **/
class BeforePreBuildJob(var appProject: Project) {
    private val isRunApp by lazy {
        !isRunAssembleTask(appProject)
    }

    companion object {
        const val TRANSFORMS = "/intermediates/transforms/"
        const val JAR_HASHES = "/intermediates/dex_archive_input_jar_hashes/"
        const val DATABIND_DENPEDENCY =
            "/intermediates/data_binding_base_class_logs_dependency_artifacts/"
        const val PRE = "pre"
        const val Build = "Build"
    }

    fun runCleanAction() {
        val android = appProject.extensions.getByType(AppExtension::class.java)

        android.applicationVariants.forEach {
            getTaskProvider(
                PRE + it.flavorName.replaceFirstChar() + it.buildType.name.replaceFirstChar() + Build
            )?.let { task ->
                innerRunCleanAction(task, it.buildType.name, it.flavorName)
            }
        }

    }


    private fun getTaskProvider(taskname: String): TaskProvider<Task>? {

        var bundleTask: TaskProvider<Task>? = null
        try {
            bundleTask = appProject.tasks.named(taskname)
        } catch (ignored: Exception) {
        }
        return bundleTask
    }


    private fun innerRunCleanAction(
        task: TaskProvider<Task>, buildType: String, flavor: String? = null
    ) {
        //清理 jar
        task.configure {
            it.doFirst {

                if (isRunApp) {
                    CleanDuplicateAction().let { it1 ->
                        it1.job = this@BeforePreBuildJob
                        it1.flavor = flavor
                        it1.buildType = buildType
                        it1.clean()
                    }
                }

                FilePermissionAction().let { it1 ->
                    it1.job = this@BeforePreBuildJob
                    it1.flavor = flavor
                    it1.buildType = buildType
                    it1.modify()
                }
            }
        }
    }


    open class CleanDuplicateAction {
        lateinit var job: BeforePreBuildJob
        var flavor: String? = null
        lateinit var buildType: String

        fun clean() {
            val destDir = File(job.appProject.buildDir.absolutePath, TRANSFORMS)
            if (destDir.exists()) {
                destDir.deleteRecursively()
            }
        }
    }

    open class FilePermissionAction {
        lateinit var job: BeforePreBuildJob
        var flavor: String? = null
        lateinit var buildType: String

        fun modify() {
            val variant = (flavor ?: "") + buildType.replaceFirstChar()
            val destDir = File(
                job.appProject.buildDir.absolutePath,
                DATABIND_DENPEDENCY + variant + File.separator + "out"
            )
            if (destDir.exists()) {
                destDir.deleteRecursively()
            }
        }
    }

}