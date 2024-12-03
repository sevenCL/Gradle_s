package dependencies

import com.android.build.gradle.AppExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import utils.replaceFirstChar
import java.util.Locale

/**
 * @author seven
 * @date 2024/8/28
 * @desc
 **/
open class ChildProjectDependencies(
    var project: Project,
    private var android: AppExtension,
    private var allChangedProject: MutableMap<String, Project>
) {
    private var ALL_SUFFIX = arrayOf("implementation", "api", "compileOnly")
    var allConfigList = arrayListOf<Configuration>()

    init {
        //生成所有的 config ，project 的所有依赖分散到 各个 config 中去
        ALL_SUFFIX.forEach {
            val configuration = project.configurations.maybeCreate(it)
            allConfigList.add(configuration)
        }

        android.applicationVariants.forEach {
            ALL_SUFFIX.forEach { suffix ->
                //组合的 Config
                val fullConfigName =
                    it.flavorName + it.buildType.name.replaceFirstChar() + suffix.replaceFirstChar()
                val fullConfiguration = project.configurations.maybeCreate(fullConfigName)
                if (!allConfigList.contains(fullConfiguration)) {
                    allConfigList.add(fullConfiguration)
                }

                //单独的 flavorConfig
                val flavorConfigName = it.flavorName + suffix.replaceFirstChar()
                val flavorConfiguration = project.configurations.maybeCreate(flavorConfigName)
                if (!allConfigList.contains(flavorConfiguration)) {
                    allConfigList.add(flavorConfiguration)
                }

                //单独的 buildconfig
                val buildTypeConfigName = it.buildType.name + suffix.replaceFirstChar()
                val buildTypeConfiguration = project.configurations.maybeCreate(buildTypeConfigName)
                if (!allConfigList.contains(buildTypeConfiguration)) {
                    allConfigList.add(buildTypeConfiguration)
                }
            }
        }

    }

    // 开始处理依赖关系
    open fun doDependencies(dependenciesHelper: DependenciesHelper,needIncludeList:ArrayList<String>) {
        //当前的 project 是否为改变的
        val isCurProjectChanged: Boolean =
            allChangedProject.get(key = project.path) != null
        //如果当前project 没有做改动，需要把自身变成 aar 给到 parent project
        if (!isCurProjectChanged) {
            needIncludeList.add(project.name)
            dependenciesHelper.modifyDependencies(this)
        }
    }
}