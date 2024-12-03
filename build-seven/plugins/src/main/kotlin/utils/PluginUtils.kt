package utils

import com.android.build.api.transform.Transform
import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import db.TransformsConstants
import org.gradle.api.Project
import org.gradle.plugins.ide.idea.IdeaPlugin
import org.gradle.plugins.ide.idea.model.IdeaModule
import java.util.Locale
import kotlin.reflect.jvm.isAccessible

/**
 * @author seven
 * @date 2024/8/28
 * @desc
 **/

/**
 * idea 插件不下载源码
 */
fun speedSync(appProject: Project){
    appProject.rootProject.allprojects { p ->
        val ideaPlugin = p.plugins.findPlugin(IdeaPlugin::class.java)
        if (ideaPlugin != null) {
            val ideaModule: IdeaModule? = ideaPlugin.model?.module
            ideaModule?.isDownloadSources = false
        }
    }
}

//判断是否子 project 的
fun hasAndroidPlugin(curProject: Project): Boolean {
    return curProject.plugins.hasPlugin("com.android.library")
}

//判断是否子 project 的
fun hasAppPlugin(curProject: Project): Boolean {
    return curProject.plugins.hasPlugin("com.android.application")
}

//判断是否java project 的
fun hasJavaPlugin(curProject: Project): Boolean {
    return curProject.plugins.hasPlugin("java-library")
}

/**
 * 判断是否是当前项目
 */
fun isCurProjectRun(appProject: Project): Boolean {
    var ret = false
    var projectPath = ""
    val arg = appProject.gradle.startParameter.taskRequests.getOrNull(0)?.args?.getOrNull(0)
    if (!arg.isNullOrEmpty()) {
        var index = arg.indexOf(SevenPlugin.ASSEMBLE)
        index = if (index > 0) index - 1 else 0
        projectPath = arg.substring(0, index)
    }
    if (projectPath.isNotEmpty()) {
        //使用 app 直接 run，currentDir 为项目目录没法使用，只能通过 截取 arg
        ret = appProject.path.equals(projectPath)
    }
    // 使用 assembledebug 命令需要这么区分
    if (appProject.gradle.startParameter.currentDir.absolutePath.equals(appProject.projectDir.absolutePath)) {
        ret = true
    }

    return ret
}

//通过 startParameter 获取  FlavorBuildType
fun getFlavorBuildType(appProject: Project): String {
    var flavorBuildType = ""
    val arg = appProject.gradle.startParameter.taskRequests.getOrNull(0)?.args?.getOrNull(0)
    if (!arg.isNullOrEmpty()) {
        var index = arg.indexOf(SevenPlugin.ASSEMBLE)
        index = if (index > -1) index + SevenPlugin.ASSEMBLE.length else 0
        flavorBuildType = arg.substring(index, arg.length)
    }
    if (flavorBuildType.isNotEmpty()) {
        flavorBuildType = flavorBuildType.substring(0, 1).lowercase(Locale.ROOT) + flavorBuildType.substring(1)
    }
    return flavorBuildType
}

fun isRunAssembleTask(curProject: Project): Boolean {
    return curProject.projectDir.absolutePath.equals(curProject.gradle.startParameter.currentDir.absolutePath)
}

fun speedBuildByOption(appProject: Project, appExtension: AppExtension) {
    //禁用 xxx transform,不影响 app 运行
//    val transformsFiled = BaseExtension::class.members.firstOrNull { it.name == "_transforms" }
//    var excludeTransForms: List<String>? = null
//    try {
//        excludeTransForms = (appProject.property("excludeTransForms") as? String)?.split(" ")
//    } catch (ignore: Exception) {
//    }
//
//    if (transformsFiled != null) {
//        transformsFiled.isAccessible = true
//        val xValue = transformsFiled.call(appExtension) as? MutableList<Transform>
//        xValue?.removeAll {
//            TransformsConstants.TRANSFORM.contains(it.name) || (excludeTransForms?.contains(it.name) ?: false)
//        }
//
//        if ((xValue?.size ?: 0) > 0) {
//            println("SevenPlugin : the following transform were detected : ")
//            xValue?.forEach {
//                println("transform: " + it.name)
//            }
//            println("SevenPlugin : you can disable it to speed up by this way：")
//            println("transFormList = [\"" + xValue!![0].name + "\"]")
//        }
//    }

    boostGradleOption(appProject)
}

fun boostGradleOption(appProject: Project) {

    settingProperty(appProject)

    //并行编译
    appProject.gradle.startParameter.isParallelProjectExecutionEnabled = true
    //最大线程数
    appProject.gradle.startParameter.maxWorkerCount += 4
}

private fun settingProperty(project: Project){
    val map= hashMapOf(
        "org.gradle.parallel" to true,//并行编译
        "org.gradle.daemon" to true,//守护进程（复用gradle进程）
        "org.gradle.configureondemand" to true,//按需配置
        //开启kotlin增量编译和并行编译
        "kotlin.incremental" to true,
        "kotlin.incremental.java" to true,
        "kotlin.incremental.js" to true,
        "kotlin.caching.enabled" to true,
        "kotlin.parallel.tasks.in.project" to true,
        //优化kapt
        "kapt.use.worker.api" to true,//并行运行kapt1.2.60版本以上支持
        "kapt.incremental.apt" to true,//增量编译 kapt1.3.30版本以上支持
        "kapt.include.compile.classpath" to false,//kapt avoiding 如果用kapt依赖的内容没有变化，会完全重用编译内容，省掉最上图中的:app:kaptGenerateStubsDebugKotlin的时间
        "kapt.classloaders.cache.size" to 5,//类加载缓存数5
        "org.gradle.caching" to true,//构建缓存
        "android.enableBuildCache" to true,//开启缓存编译
        "org.gradle.configuration-cache" to true,//配置缓存
    )
    map.forEach {
        if (project.hasProperty(it.key).not())
            project.rootProject.extensions.extraProperties.set(it.key,it.value)
    }
}