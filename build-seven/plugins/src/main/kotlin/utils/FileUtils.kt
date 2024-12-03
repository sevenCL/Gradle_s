package utils

import bean.ModuleChangeTime
import bean.ModuleChangeTimeList
import com.google.gson.Gson
import db.Constants
import org.gradle.api.Project
import java.io.File

/**
 * @author seven
 * @date 2024/8/28
 * @desc
 **/
object FileUtils {
    private lateinit var project: Project

    internal fun attach(p: Project) {
        project = p
    }

    internal fun findFirstLevelAarPath(project: Project): String? {
        val dir = File(project.buildDir.absolutePath + "/outputs/aar/")
        if (dir.exists()) {
            val files = dir.listFiles { _, name -> name?.endsWith(".aar") ?: false }
            return if (!files.isNullOrEmpty()) files[0].absolutePath else null
        }
        return null
    }

    internal fun findFirstLevelJarPath(project: Project): String? {
        val dir = File(project.buildDir.absolutePath + "/libs/")
        if (dir.exists()) {
            val files = dir.listFiles { _, name -> name?.endsWith(".jar") ?: false }
            return if (!files.isNullOrEmpty()) files[0].absolutePath else null
        }
        return null
    }

    internal fun getLocalCacheDir(): String {
        val appFolder = "." + getFlatAarName(project)
        return project.rootProject.rootDir.absolutePath + File.separator + ".gradle" + File.separator + ".sevenPluginCache" + File.separator + appFolder + File.separator
    }

    private fun getAarLibsPath(): String {
        return project.rootProject.rootDir.absolutePath + File.separator  + "aarLibs" + File.separator
    }

    internal fun getAarLibsPath(project: Project): String {
        return getAarLibsPath() + project.name + File.separator
    }

    internal fun getApkLocalPath(): String {
        var filepath = ""
        File(project.buildDir.absolutePath + File.separator).walkTopDown().forEach {
            if (it.absolutePath.endsWith(".apk")) {
                filepath = it.absolutePath
                return@forEach
            }
        }
        return filepath
    }

    /**
     * 将有变动的module信息写入文件
     */
    fun File.writeFileToModuleJson(moduleChangeList: MutableList<ModuleChangeTime>) {
        val newJsonTxt = Gson().toJson(ModuleChangeTimeList(moduleChangeList))
        this.writeText(newJsonTxt)
        LogUtils.d("writeFileToModuleJson success!")
    }

    /**
     * 获取已经存储的module
     */
    fun getLocalModuleChange(): File? {
        val dir = File(getLocalCacheDir())
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val jsonFile = File(dir, Constants.MODULE_CHANGE_TIME)
        return if (jsonFile.exists()) {
            jsonFile
        } else {
            null
        }
    }


    /**
     * 文件遍历
     */
    fun File.eachFileRecurse(closure: ((File) -> Boolean)?) {
        listFiles()?.let {
            for (file in it) {
                if (file.isDirectory) {
                    val continueRecursion = closure?.invoke(file) ?: true
                    if (continueRecursion) {
                        file.eachFileRecurse(closure)
                    }
                } else {
                    closure?.invoke(file)
                }
            }
        }
    }

    //不能通过name ，需要通过 path ，有可能有多级目录(: 作为aar名字会有冲突不能用)
    fun getFlatAarName(project: Project): String {
        return project.path.substring(1).replace(":", "-")
    }

    fun flatDirs(appProject: Project) {
        val map = mutableMapOf<String, File>()
        map["dirs"] = File(getLocalCacheDir())
        appProject.rootProject.allprojects {
            LogUtils.d(" flatDirs-> $it $map ")
            it.repositories.flatDir(map)
        }
    }
}