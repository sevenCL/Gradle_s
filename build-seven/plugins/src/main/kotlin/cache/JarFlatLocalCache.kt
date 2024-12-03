package cache

import SevenPlugin
import org.gradle.api.Project
import utils.FileUtils
import utils.FileUtils.getFlatAarName
import java.io.File

/**
 * @author seven
 * @date 2024/8/28
 * @desc
 **/
class JarFlatLocalCache(
    private var childProject: Project,
    var sevenPlugin: SevenPlugin,
    var allChangedProject: MutableMap<String, Project>
): LocalCache() {
    companion object {
        const val JAR = "jar"
    }

    override fun uploadLocalCache() {
        //通过 flat copy 到cache 目录
        val localCacheTask = childProject.tasks.maybeCreate("uploadLocalCache").let {
            it.doLast {
                val flatAarName = getFlatAarName(childProject)
                val inputPath = FileUtils.findFirstLevelJarPath(childProject)
                val outputFile = File(FileUtils.getLocalCacheDir(), "$flatAarName.jar")

                inputPath?.let { it1->
                    if (outputFile.exists()) {
                        outputFile.delete()
                    }
                    File(it1).copyTo(outputFile, true)
                    putIntoLocalCache(flatAarName, "$flatAarName.jar")
                }
            }
        }
        childProject.tasks.findByPath(JAR)?.finalizedBy(localCacheTask)
    }
}