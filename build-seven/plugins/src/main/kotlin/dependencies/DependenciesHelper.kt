package dependencies

import bean.SevenBean
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency
import org.gradle.api.internal.artifacts.dependencies.DefaultSelfResolvingDependency
import org.gradle.api.internal.artifacts.publish.DefaultPublishArtifact
import org.gradle.api.internal.file.collections.DefaultConfigurableFileCollection
import org.gradle.api.internal.file.collections.DefaultConfigurableFileTree
import utils.FileUtils
import utils.FileUtils.getFlatAarName
import utils.hasAndroidPlugin
import java.io.File

/**
 * @author seven
 * @date 2024/8/28
 * @desc
 **/
class DependenciesHelper(
    private val sevenBean: SevenBean,
    private val allChangedProject: MutableMap<String, Project>? = null,
    private var projectDependenciesList: MutableList<ChildProjectDependencies>
) {

    //获取第一层 parent 依赖当前 project
    private fun getFirstLevelParentDependencies(project: Project): MutableMap<Project, MutableList<Configuration>> {
        val parentProjectList = mutableMapOf<Project, MutableList<Configuration>>()
        projectDependenciesList.forEach {
            val parentProject = it.project
            //子project 所有的 config
            it.allConfigList.forEach { config ->
                //每一个config 所有依赖
                run loop@{
                    config.dependencies.forEach { dependency ->
                        //项目依赖
                        if (dependency is DefaultProjectDependency && dependency.name.equals(project.name)) {
                            parentProjectList[parentProject]?.apply {
                                this.add(config)
                            } ?: let {
                                val configList = mutableListOf<Configuration>()
                                configList.add(config)
                                parentProjectList.put(parentProject, configList)
                            }
                            //每一个 config 对同个 project 重复依赖是无意义，可直接 return
                            return@loop
                        }
                    }
                }
            }
        }
        return parentProjectList
    }

    /**
     * 解决各个 project 变动之后需要打成 aar 包，算法V1
     */
    fun modifyDependencies(projectWapper: ChildProjectDependencies) {
        //找到所有的父依赖
        val map = getFirstLevelParentDependencies(projectWapper.project)
        //找到当前所有通过 artifacts.add("default", file('xxx.aar')) 依赖进来的 aar,并构建local cache
        val artifactAarList = getAarByArtifacts(projectWapper.project)
        //可能有多个父依赖，所以需要遍历
        map.forEach { parentProject ->

            artifactAarList.forEach {
                addAarDependencyToProject(
                    it,
                    parentProject.key.configurations.maybeCreate("api").name,
                    parentProject.key
                )
            }

            //父依赖的 configuration 添加 当前的 project 对应的aar
            parentProject.value.forEach { parentConfig ->
                val find = allChangedProject?.get(parentProject.key.path)
                if (find == null) {
                    // 剔除原有的依赖
                    parentConfig.dependencies.removeAll { dependency ->
                        dependency is DefaultProjectDependency && dependency.name.equals(
                            projectWapper.project.name
                        )
                    }

                    //android  module or artifacts module
                    if (hasAndroidPlugin(projectWapper.project) || artifactAarList.size > 0) {
                        addAarDependencyToProject(
                            getFlatAarName(projectWapper.project),
                            parentConfig.name,
                            parentProject.key
                        )
                    } else {
                        //java module
                        addJarDependencyToProject(
                            getFlatAarName(projectWapper.project),
                            parentConfig.name,
                            parentProject.key
                        )
                    }

                    // 把子 project 自身的依赖全部 给到 父 project
                    projectWapper.allConfigList.forEach { childConfig ->
                        childConfig.dependencies.forEach { childDepency ->
                            if (childDepency is DefaultProjectDependency) {
                                if (childDepency.targetConfiguration == null) {
                                    childDepency.targetConfiguration = "default"
                                }
                                // Android Studio 4.0.0 索引//
                                val dependencyClone = childDepency.copy()
                                dependencyClone.targetConfiguration = null
                                // parent 铁定有 childConfig.name 的 config
                                parentProject.key.dependencies.add(
                                    childConfig.name,
                                    dependencyClone
                                )
                            } else {
                                if (childDepency is DefaultSelfResolvingDependency && (childDepency.files is DefaultConfigurableFileCollection || childDepency.files is DefaultConfigurableFileTree)) {
                                    // 这里的依赖是以下两种： 无需添加在 parent ，因为 jar 包直接进入 自身的 aar 中的libs 文件夹
                                    //    implementation rootProject.files("libs/tingyun-ea-agent-android-2.15.4.jar")
                                    //    implementation fileTree(dir: "libs", include: ["*.jar"])


                                } else {
                                    parentProject.key.dependencies.add(
                                        childConfig.name,
                                        childDepency
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    private fun addAarDependencyToProject(aarName: String, configName: String, project: Project) {
        //添加 aar 依赖 以下代码等同于 api/implementation/xxx file("xxx.aar"),源码使用 linkedMap
        val path = FileUtils.getLocalCacheDir() + aarName + ".aar"
        if (!File(path).exists()) return
        project.dependencies.add(configName, project.files(File(path)))
    }

    private fun addJarDependencyToProject(aarName: String, configName: String, project: Project) {
        //添加 jar 依赖
        val path = FileUtils.getLocalCacheDir() + aarName + ".jar"
        if (!File(path).exists()) return
//        flatDirs implementation (name:"xxx",ext:"aar")
//        val map = linkedMapOf<String, String>()
//        map.put("name", aarName)
//        map.put("ext", "jar")
//        project.dependencies.add(configName, map)
        project.dependencies.add(configName, project.files(File(path)))
    }

    private fun getAarByArtifacts(childProject: Project): MutableList<String> {
        //找到当前所有通过 artifacts.add("default", file('xxx.aar')) 依赖进来的 aar
        val listArtifact = mutableListOf<DefaultPublishArtifact>()
        val aarList = mutableListOf<String>()
        childProject.configurations.maybeCreate("default").artifacts.forEach {
            if (it is DefaultPublishArtifact && "aar" == it.type) {
                listArtifact.add(it)
            }
        }

        //拷贝一份到 缓存
        listArtifact.forEach {
            it.file.copyTo(File(FileUtils.getLocalCacheDir(), it.file.name), true)
            //剔除后缀 （.aar）
            aarList.add(removeExtension(it.file.name))
        }

        return aarList
    }


    private fun removeExtension(filename: String): String {
        val index = filename.lastIndexOf(".")
        return if (index == -1) {
            filename
        } else {
            filename.substring(0, index)
        }
    }


    /**
     * 解决各个 project 变动之后需要打成 aar 包,算法 V2
     */
//    fun modifyDependenciesV2(projectWapper: ChildProjectDependencies) {
//        //todo
//
//    }
}