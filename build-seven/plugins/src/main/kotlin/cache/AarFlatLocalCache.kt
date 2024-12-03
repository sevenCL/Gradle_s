package cache

import SevenPlugin
import com.android.build.gradle.AppExtension
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import utils.FileUtils
import utils.FileUtils.getFlatAarName
import utils.LogUtils
import utils.replaceFirstChar
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * @author seven
 * @date 2024/8/28
 * @desc
 **/
class AarFlatLocalCache(
    private var childProject: Project,
    var sevenPlugin: SevenPlugin,
    private var appProject: Project,
    private var mAllChangedProject: MutableMap<String, Project>
) : LocalCache() {
    companion object {
        const val ASSEMBLE = "assemble"
    }

    override fun uploadLocalCache() {
        // 创建一个线程池
//        val threadPoolExecutor = initThread()
        //先 hook bundleXXaar task 打出包
        val android = appProject.extensions.getByType(AppExtension::class.java)
        android.applicationVariants.forEach {
//            threadPoolExecutor.execute {
//            LogUtils.d("thread_ ${it.name}  ${Thread.currentThread().id}")
//            LogUtils.d(" getAppAssembleTask flavor ${it.flavorName.replaceFirstChar()} buildType ${it.buildType.name.replaceFirstChar()}")
            getAppAssembleTask(ASSEMBLE + it.flavorName.replaceFirstChar() + it.buildType.name.replaceFirstChar())?.let { task ->
                hookBundleAarTask(task, it.buildType.name)
            }
//            }
        }

    }

    private fun initThread(): ThreadPoolExecutor {
        /** DES: DES：取CPU核心数-1 代码来自协程内部 [kotlinx.coroutines.CommonPool.createPlainPool] */
        val corePoolSize = (Runtime.getRuntime().availableProcessors() - 1).coerceAtLeast(1)
        val threadPoolExecutor = ThreadPoolExecutor(
            corePoolSize,
            corePoolSize,
            5L,
            TimeUnit.SECONDS,
            LinkedBlockingQueue<Runnable>()
        )
        // DES：让核心线程也可以回收
        threadPoolExecutor.allowCoreThreadTimeOut(true)
        return threadPoolExecutor
    }


    private fun getAppAssembleTask(name: String): TaskProvider<Task>? {
        var taskProvider: TaskProvider<Task>? = null
        try {
            taskProvider = appProject.tasks.named(name)
        } catch (ignore: Exception) {
        }
        return taskProvider
    }

    @Synchronized
    fun hookBundleAarTask(task: TaskProvider<Task>, buildType: String) {
        //如果当前模块是改动模块，需要打 aar
        if (mAllChangedProject.contains(childProject.path)) {
            //打包aar
            val bundleTask = getBundleTask(childProject, buildType.replaceFirstChar())?.apply {
                task.configure {
                    it.finalizedBy(this)
                }
            }

            //copy aar
            val localCacheTask =
                childProject.tasks.maybeCreate(
                    "uploadLocalCache" + buildType.replaceFirstChar(),
                    FlatTask::class.java
                )
            localCacheTask.localCache = this@AarFlatLocalCache
            bundleTask?.finalizedBy(localCacheTask)
        }
    }

    //获取 gradle 里的 bundleXXXAar task , 为了就是打包每一个模块的 aar
    private fun getBundleTask(project: Project, variantName: String): Task? {
        val taskPath = "bundle" + variantName + "Aar"
        var bundleTask: TaskProvider<Task>? = null
        try {
            bundleTask = project.tasks.named(taskPath)
        } catch (ignored: Exception) {
            LogUtils.d(" ERROR getBundle${variantName}AarTask ")
        }
        return bundleTask?.get()
    }

    //需要构建 local cache
    open class FlatTask : DefaultTask() {
        @Internal
        var inputPath: String? = null

        @Internal
        var inputFile: File? = null

        @Internal
        var outputPath: String? = null

        @Internal
        var outputDir: File? = null

        @Internal
        lateinit var localCache: AarFlatLocalCache

        @TaskAction
        fun uploadLocalCache() {
            val flatAarName = getFlatAarName(project)
            this.inputPath = FileUtils.findFirstLevelAarPath(project)
            this.outputPath = FileUtils.getLocalCacheDir()
            inputFile = inputPath?.let { File(it) }
            outputDir = this.outputPath?.let { File(it) }

            inputFile?.let {
                File(outputDir, "$flatAarName.aar").let { file ->
                    if (file.exists()) {
                        file.delete()
                    }
                }
                it.copyTo(File(outputDir, "$flatAarName.aar"), true)
                localCache.putIntoLocalCache(flatAarName, "$flatAarName.aar")
            }
        }

        @TaskAction
        fun uploadAarLibsLocalCache() {
            val flatAarName = getFlatAarName(project)
            this.inputPath = FileUtils.findFirstLevelAarPath(project)
            this.outputPath = FileUtils.getAarLibsPath(project)
            inputFile = inputPath?.let { File(it) }
            outputDir = this.outputPath?.let { File(it) }

            inputFile?.let {
                File(outputDir, "$flatAarName.aar").let { file ->
                    if (file.exists()) {
                        file.delete()
                    }
                }
                it.copyTo(File(outputDir, "$flatAarName.aar"), true)
                val gradleFile = File(outputDir, "build.gradle.kts")
                if (gradleFile.exists().not())
                    gradleFile.createNewFile()
                val fos = FileOutputStream(gradleFile)
                fos.write("configurations.maybeCreate(\"default\")\nartifacts.add(\"default\", file(\"$flatAarName.aar\"))".toByteArray())
                fos.close()
            }
        }
    }
}