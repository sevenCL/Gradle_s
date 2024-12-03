package utils

import dependencies.AppProjectDependencies
import org.gradle.api.artifacts.Dependency
import org.gradle.api.internal.artifacts.dependencies.DefaultSelfResolvingDependency

/**
 * @author seven
 * @date 2024/8/28
 * @desc
 **/
object LogUtils {

    private var tag = "SevenPlugin-> "

    private var enable = true

    fun init(tag: String) {
        this.tag = tag
    }

    fun enableLog(enable: Boolean) {
        this.enable = enable
    }

    fun d(msg: String) {
        if (!enable) return
        println(">>> $tag >>>  $msg")
    }

    //打印处理完的整个依赖图
    fun printlnDependencyGraph(mAppProjectDependencies: AppProjectDependencies) {
        mAppProjectDependencies.mAllChildProjectDependenciesList.forEach { it ->
            d("project name: ${it.project.name} --- start")
            it.allConfigList.filter { it.dependencies.isNotEmpty() }.forEach { configuration ->
                d("Config name:${configuration.name}")
                configuration.dependencies.forEach {
                    d("dependency:  $it ${getFileName(it)}    ${it.hashCode()}")
                }
            }
            d("project name: ${it.project.name} --- end \n")
        }
    }

    private fun getFileName(dependency: Dependency): String {
        val buffer = StringBuffer()
        if (dependency is DefaultSelfResolvingDependency) {
            dependency.files.files.forEach {
                buffer.append("-$it")
            }
        }
        return buffer.toString()
    }
}