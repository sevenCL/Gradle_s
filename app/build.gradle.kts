plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.ksp)
    id("therouter")
//    id("com.seven.plugin")
}

android {
    namespace = "com.seven.gradle"
    compileSdk = properties["compileSdk"].toString().toInt()

    defaultConfig {
        applicationId = properties["applicationId"].toString()
        minSdk = properties["minSdk"].toString().toInt()
        targetSdk = properties["targetSdk"].toString().toInt()
        versionCode = properties["versionCode"].toString().toInt()
        versionName = properties["versionName"].toString()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    viewBinding {
        enable = true
    }

//    SevenPluginConfig {
//        openLog = true
//        //剔除模块不打成 aar
//        excludeModule = arrayOf(":xxx").toSet()
//        dexMergeIncremental = true
//    }
}

dependencies {

    implementation(project(":lib_core"))
    implementation(project(":lib_common"))
//    implementation(project(":lib_ui"))
    implementation(libs.seven.lib.ui)
    implementation(project(":data_common"))
    implementation(project(":module_user"))
    implementation(project(":module_home"))
    implementation(project(":module_reviews"))
    ksp(libs.the.router.apt)
    implementation(libs.the.router)
}

/**
 * task的创建方式
 */
tasks.create("printCreate") {
    println("printCreate")
}
tasks.register("printRegister") {
    println("printRegister")
}
tasks.register("printRegister2") {
    println("printRegister2")
}

/**
 * 自定义task
 */
open class SevenTask : DefaultTask() {

    @Internal
    var taskName = "default"

    @TaskAction
    fun action1() {
        println("$taskName action1")
    }

    @TaskAction
    fun action2() {
        println("$taskName action2")
    }
}
tasks.register("sevenTask", SevenTask::class.java) {
    taskName = "curr taskName -> "
}

/**
 * 增量task
 */
open class CopyTask : DefaultTask() {

    @InputFiles
    lateinit var from: FileCollection

    @OutputDirectory
    lateinit var to: Directory

    @TaskAction
    fun execute() {
        val file = from.singleFile
        if (file.isDirectory) {
            from.asFileTree.forEach {
                copyFile2Dir(it, to)
            }
        } else
            copyFile2Dir(file, to)
    }

    private fun copyFile2Dir(src: File, dir: Directory) {
        val dest = File("${dir.asFile.path}/${src.name}")
        src.copyTo(dest, true)
    }
}
tasks.register("copyTask", CopyTask::class.java) {
    from = files("from")
    to = layout.projectDirectory.dir("to")
}

/**
 * 增量action
 */
open class CopyActionTask : DefaultTask() {

    @Incremental
    @InputFiles
    lateinit var from: FileCollection

    @OutputDirectory
    lateinit var to: Directory

    @TaskAction
    fun execute(inputChanges: InputChanges) {

        val incremental = inputChanges.isIncremental
        println("CopyActionTask-> incremental = $incremental")

        inputChanges.getFileChanges(from).forEach {
            if (it.fileType != FileType.DIRECTORY) {
//                enum class ChangeType {
//                    ADDED,
//                    MODIFIED,
//                    REMOVED
//                }
                val changeType = it.changeType
                val changeFileName = it.file.name
                println("CopyActionTask-> changeType = $changeType changeFileName = $changeFileName")

                if (changeType != ChangeType.REMOVED)
                    copyFile2Dir(it.file, to)
                else
                    deleteFile(it.file, to)
            }
        }

        val file = from.singleFile
        if (file.isDirectory) {
            from.asFileTree.forEach {
                copyFile2Dir(it, to)
            }
        } else
            copyFile2Dir(file, to)
    }

    private fun copyFile2Dir(src: File, dir: Directory) {
        val dest = File("${dir.asFile.path}/${src.name}")
        src.copyTo(dest, true)
    }

    private fun deleteFile(src: File, dir: Directory) {
        val dest = File("${dir.asFile.path}/${src.name}")
        println("CopyActionTask-> delete path ${dest.path}")
        dest.delete()
    }
}
tasks.register("copyActionTask", CopyActionTask::class.java) {
    from = files("from")
    to = layout.projectDirectory.dir("to")
}

///**
// * 插件扩展对象
// */
//open class TestAndroid internal constructor(project: Project) {
//    var title: String? = null
//    var content: String? = null
//    var options: TestAndroidOptions =
//        project.extensions.create("options", TestAndroidOptions::class)
//}
//
//open class TestAndroidOptions {
//    var version: String? = null
//}
//
//project.extensions.create("testAndroid", TestAndroid::class.java)
