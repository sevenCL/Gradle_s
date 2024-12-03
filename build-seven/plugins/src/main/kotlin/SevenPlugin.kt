import bean.SevenBean
import com.android.build.gradle.AppExtension
import dependencies.AppProjectDependencies
import listener.SevenBuildListener
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.build.event.BuildEventsListenerRegistry
import service.BuildListenerService
import utils.ChangeUtils
import utils.FileUtils
import utils.LogUtils
import utils.getFlavorBuildType
import utils.hasAndroidPlugin
import utils.isCurProjectRun
import utils.speedBuildByOption
import utils.speedSync
import java.util.Locale
import javax.inject.Inject

/**
 * @author seven
 * @date 2024/8/28
 * @desc
 */
abstract class SevenPlugin : Plugin<Project> {

    companion object {
        const val ASSEMBLE = "assemble"
    }

    private lateinit var sevenBean: SevenBean
    private lateinit var project: Project
    private lateinit var appProjectDependencies: AppProjectDependencies

    private val flavorBuildType by lazy {
        getFlavorBuildType(project)
    }

    private val allChangedProject by lazy {
        ChangeUtils.getChangeModuleMap(project)
    }

    @Inject
    abstract fun getEventsListenerRegistry(): BuildEventsListenerRegistry

    override fun apply(project: Project) {
        LogUtils.d("apply Project ${project.name}")

        project.extensions.create("SevenPluginConfig", SevenBean::class.java)

        speedSync(project)

        if (hasAndroidPlugin(project) || !isCurProjectRun(project)) {
            return
        }

        val serviceProvider = project.gradle.sharedServices.registerIfAbsent(
            "taskEvents",
            BuildListenerService::class.java
        ) {
            //configure service here
        }
        getEventsListenerRegistry().onTaskCompletion(serviceProvider)
        serviceProvider.get().test()

        this.project = project

        //禁止 release 使用加速插件
        if (flavorBuildType.lowercase(Locale.ROOT).contains("release")) {
            return
        }

        FileUtils.attach(project)

        project.afterEvaluate {
            this.sevenBean = project.property("SevenPluginConfig") as SevenBean
            LogUtils.d(" $sevenBean ${sevenBean.openLog}")
            LogUtils.enableLog(sevenBean.openLog)

            sevenBean.excludeModule.forEach {
                project.rootProject.findProject(it)?.run {
                    allChangedProject[it] = this
                }
            }

            project.gradle.addBuildListener(
                SevenBuildListener(
                    this@SevenPlugin,
                    project,
                    allChangedProject,
                    sevenBean.dexMergeIncremental
                )
            )

//            project.gradle.projectsEvaluated{
//
//            }

            val appExtension = project.extensions.getByType(AppExtension::class.java)

            //开启一些加速的编译项
            speedBuildByOption(project, appExtension)

            appProjectDependencies =
                AppProjectDependencies(
                    project,
                    appExtension,
                    sevenBean,
                    allChangedProject
                ) { _, _ ->
//                ) { finish, includeList ->
                    LogUtils.printlnDependencyGraph(appProjectDependencies)
                }
        }
    }
}