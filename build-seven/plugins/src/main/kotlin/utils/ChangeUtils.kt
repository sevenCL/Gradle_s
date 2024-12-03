package utils

import bean.ModuleChangeTime
import bean.ModuleChangeTimeList
import com.google.gson.Gson
import db.Constants
import org.gradle.api.Project
import utils.FileUtils.eachFileRecurse
import utils.FileUtils.writeFileToModuleJson
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

/**
 * @author seven
 * @date 2024/8/28
 * @desc
 **/
object ChangeUtils {

    //Gradle 静态变量会被保留
    private val newModuleList: MutableList<ModuleChangeTime> = mutableListOf()

    /**
     * 获取发生变动的module信息
     */
    fun getChangeModuleMap(project: Project): MutableMap<String, Project> {
        val changeMap: MutableMap<String, Project> = mutableMapOf()
        val startTime = System.currentTimeMillis()

        getNewModuleList(project)

        val localModuleList = FileUtils.getLocalModuleChange()

        localModuleList?.let { localFile ->
            try {
                val oldModuleList =
                    Gson().fromJson(localFile.readText(), ModuleChangeTimeList::class.java)
                // 返回null, 代表之前没有编译过，要重新编译
                if (oldModuleList.list.isNullOrEmpty()) {
                    allProjectsChange(project, changeMap)
                } else {
                    newModuleList.forEach { newModule ->
                        oldModuleList.list.firstOrNull { newModule.moduleName == it.moduleName }
                            .also { moduleChange ->
                                // 为null, 代表这个module是新创建的
                                if (moduleChange == null) {
                                    project.rootProject.allprojects.firstOrNull { pt ->
                                        pt?.path == newModule.moduleName
                                    }?.let {
                                        changeMap[newModule.moduleName] = it
                                        LogUtils.d(" 你添加了=${newModule.moduleName}        ")
                                    }
                                }
                                // 已有的module 文件发生改变
                                else if (moduleChange.changeTag != newModule.changeTag) {
                                    changeMap[newModule.moduleName] =
                                        project.rootProject.allprojects.first { it?.path == newModule.moduleName }
                                    LogUtils.d(" 你修改了=${newModule.moduleName}      ")
                                }
                            }
                    }
                }
            } catch (e: Exception) {
                LogUtils.d(" ERROR getChangeModuleMap ${e.message} ")
            }
        } ?: run {
            allProjectsChange(project, changeMap)
        }

        //最后补一个 app 的 module，app 是认为做了改变，不打成 aar
        changeMap[project.path] = project
        LogUtils.d("count time====>>>> ${System.currentTimeMillis() - startTime}ms   " + changeMap.toString())
        return changeMap
    }

    /**
     * 如果没有这个文件的话，认为整个模块都做了改动
     */
    private fun allProjectsChange(project: Project, changeMap: MutableMap<String, Project>) {
        project.rootProject.allprojects.filter { it != project.rootProject && it.childProjects.isEmpty() }
            .forEach {
                changeMap[it.path] = it
            }
    }

    /**
     *  获取当前module和文件时间戳
     */
    private fun getNewModuleList(project: Project) {
        newModuleList.clear()
        var count = 0
        var isCodeFile: Boolean
        project.rootProject.allprojects.onEach {
            if (it == project.rootProject || it.childProjects.isNotEmpty()) {
                return@onEach
            }
            var countTime = 0L
            var countSize = 0L
            it.projectDir.eachFileRecurse { file ->
                // 过滤掉build目录及该目录下的所有文件
                isCodeFile = !(file.isDirectory && Constants.BUILD == file.name)
                if (isCodeFile) {
                    countTime += file.lastModified()
                    countSize += file.length()
//                    getFileSHA256(file)
                    count++
                }
                return@eachFileRecurse isCodeFile
            }
            newModuleList.add(ModuleChangeTime(it.path, "${it.path}-$countTime-$countSize"))
        }
        LogUtils.d("total file num ====>>>> $count")
    }

    fun getFileSHA256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val fis = FileInputStream(file)
        val buffer = ByteArray(1024)
        var read = fis.read(buffer)
        while (read != -1) {
            digest.update(buffer, 0, read)
            read = fis.read(buffer)
        }
        fis.close()
        val hashBytes = digest.digest()
        val hashString = hashBytes.joinToString("") { "%02x".format(it) }
        return hashString
    }

    fun flushJsonFile() {
        val dir = File(FileUtils.getLocalCacheDir())
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val jsonFile = File(dir, Constants.MODULE_CHANGE_TIME)
        if (!jsonFile.exists()) {
            jsonFile.createNewFile()
        }
        jsonFile.writeFileToModuleJson(newModuleList)
    }
}