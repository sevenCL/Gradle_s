package task

import SevenPlugin
import com.android.build.gradle.AppExtension
import com.android.ddmlib.AndroidDebugBridge
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import utils.FileUtils
import utils.LogUtils
import utils.isRunAssembleTask
import utils.replaceFirstChar
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * @author seven
 * @date 2024/8/28
 * @desc
 **/
class InstallApkByAdb(private val appProject: Project) {

    fun maybeInstallApkByAdb() {
        if (isRunAssembleTask(appProject)) {
            val android = appProject.extensions.getByType(AppExtension::class.java)
            val installTask =
                appProject.tasks.maybeCreate("sevenInstallTask", InstallApkTask::class.java)
            installTask.android = android
            android.applicationVariants.forEach {
                getAppAssembleTask(
                    SevenPlugin.ASSEMBLE + it.flavorName.replaceFirstChar() + it.buildType.name.replaceFirstChar()
                )?.let { taskProvider ->
                    taskProvider.configure {it1->
                        it1.finalizedBy(installTask)
                    }
                }
            }
        }
    }

    private fun getAppAssembleTask(name: String): TaskProvider<Task>? {
        var taskProvider: TaskProvider<Task>? = null
        try {
            taskProvider = appProject.tasks.named(name)
        } catch (ignore: Exception) {
        }
        return taskProvider
    }

    open class InstallApkTask : DefaultTask() {
        @Internal
        lateinit var android: AppExtension

        @TaskAction
        fun installApk() {
            val adb = android.adbExecutable.absolutePath

            try {
                AndroidDebugBridge.init(false)
                val bridge = AndroidDebugBridge.createBridge(
                    android.adbExecutable.path, false,
                    Long.MAX_VALUE,
                    TimeUnit.MILLISECONDS
                )
                var firstLocalDeviceSerialNum = ""
                run loop@{
                    bridge?.devices?.forEach {
                        if (!it.serialNumber.isNullOrEmpty()) {
                            firstLocalDeviceSerialNum = it.serialNumber
                            return@loop
                        }
                    }
                }
                if (firstLocalDeviceSerialNum.isEmpty().not()) {

                    project.exec {
                        it.commandLine(
                            adb,
                            "-s",
                            firstLocalDeviceSerialNum,
                            "install",
                            "-r",
                            FileUtils.getApkLocalPath()
                        )
                    }
                    // adb -s <ip:port> install -r <app.apk>
                    // adb -s <ip:port> shell monkey -p <包名> -c android.intent.category.LAUNCHER 1
                    project.exec {
                        it.commandLine(
                            adb,
                            "-s",
                            firstLocalDeviceSerialNum,
                            "shell",
                            "monkey",
                            "-p",
                            android.defaultConfig.applicationId,
                            "-c",
                            "android.intent.category.LAUNCHER",
                            "1"
                        )
                    }
                }
            } catch (e: Exception) {
                LogUtils.d("install fail:$e")
            }
        }
    }
}