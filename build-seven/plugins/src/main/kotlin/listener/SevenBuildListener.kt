package listener

import SevenPlugin
import com.android.build.gradle.LibraryExtension
import cache.AarFlatLocalCache
import cache.JarFlatLocalCache
import cache.LocalCache
import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.TaskState
import task.InstallApkByAdb
import utils.LogUtils
import utils.hasAppPlugin
import utils.hasJavaPlugin
import java.text.SimpleDateFormat
import java.util.Calendar

/**
 * @author seven
 * @date 2024/8/28
 * @desc
 **/
class SevenBuildListener(
    private val sevenPlugin: SevenPlugin,
    private val appProject: Project,
    private val allChangedProject: MutableMap<String, Project>,
    private val dexMergeIncremental: Boolean
): BuildListener, TaskExecutionListener {

    private var taskStartTime: Long = 0
    private var taskEndTime: Long = 0
    private var buildStartTime: Long = 0

    private val stringBuilder = StringBuilder()

    @Suppress("SimpleDateFormat")
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd :hh:mm:ss")


    init {
        buildStartTime = System.currentTimeMillis()
        stringBuilder.append("\n")
        stringBuilder.append("构建开始时间：" + dateFormat.format(Calendar.getInstance().time) + "\n")
    }

    //在Project插件中，注册时机已晚，settingsEvaluated 不会回调
    override fun settingsEvaluated(target: Settings) {
        LogUtils.d(" settingsEvaluated $target")
    }

    //在Project插件中，注册时机已晚，settingsEvaluated 不会回调
    override fun projectsLoaded(gradle: Gradle) {
        LogUtils.d(" projectsLoaded $gradle")
    }

    override fun projectsEvaluated(gradle: Gradle) {
        LogUtils.d(" projectsEvaluated $gradle")
//        if (!dexMergeIncremental) {
//            BeforePreBuildJob(appProject).runCleanAction()
//        }
        appProject.rootProject.allprojects.forEach {
            //剔除 app 和 rootProject
            if (hasAppPlugin(it) || it == appProject.rootProject || it.childProjects.isNotEmpty()) {
                return@forEach
            }
            //没有改动的模块 不执行
            if (allChangedProject.contains(it.path).not()) {
                return@forEach
            }
            var mLocalCache: LocalCache? = null
            val childProject = it.project
            var childAndroid: LibraryExtension? = null
            try {
                childAndroid = it.project.extensions.getByType(LibraryExtension::class.java)
            } catch (ignore: Exception) {
            }
            //android 子 module
            if (childAndroid != null) {
                mLocalCache = AarFlatLocalCache(childProject, sevenPlugin, appProject, allChangedProject)
            } else if (hasJavaPlugin(childProject)) {
                //java 子 module
                mLocalCache = JarFlatLocalCache(childProject, sevenPlugin, allChangedProject)
            }
            //需要上传到 localCache
            mLocalCache?.uploadLocalCache()
        }

//        InstallApkByAdb(appProject).maybeInstallApkByAdb()
    }

    /**
     * 构建完成回调
     */
    @Deprecated("This method is not supported when configuration caching is enabled")
    override fun buildFinished(result: BuildResult) {
//        ChangeUtils.flushJsonFile()
//        stringBuilder.append("构建结束时间：" + dateFormat.format(Calendar.getInstance().time) + "\n")
//        val totalTime = (System.currentTimeMillis() - buildStartTime)
//        stringBuilder.append("构建总耗时：" + totalTime + "ms")
//        LogUtils.d("   $stringBuilder = ${TimeUtils.getNoMoreThanDigits(totalTime / 1000.00)}s")
    }

    /**
     * 任务执行开始
     * This method is called immediately before a task is executed.
     * @param task The task about to be executed. Never null.
     */
    @Deprecated("This method is not supported when configuration caching is enabled")
    override fun beforeExecute(task: Task) {
//        taskStartTime = System.currentTimeMillis()
    }

    /**
     * @param task The task which was executed. Never null.
     * @param state The task state. If the task failed with an exception, the exception is available in this
     * state. Never null.
     */
    @Deprecated("This method is not supported when configuration caching is enabled")
    override fun afterExecute(task: Task, state: TaskState) {
//        taskEndTime = System.currentTimeMillis()
//        if (task.name.startsWith(SevenPlugin.ASSEMBLE) && state.failure == null) {
//            LogUtils.d("task==>${task.name}, state=${state.failure}")
//            ChangeUtils.flushJsonFile()
//        }
    }

}