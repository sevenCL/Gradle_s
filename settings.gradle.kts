import org.gradle.tooling.events.FailureResult
import org.gradle.tooling.events.FinishEvent
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

pluginManagement {
    includeBuild("build-seven")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
        maven("http://127.0.0.1:8081/repository/private-release/")
    }
}

rootProject.name = "Gradle_S"
include(":app")
include(":lib_core")
include(":lib_net")
include(":lib_common")
include(":lib_ui")
include(":data_home")
include(":data_reviews")
include(":data_common")
include(":module_home")
include(":module_reviews")
include(":module_user")

include(":aarLibs:myLibrary")

apply(from = "ChangeSettingsPlugin.gradle")

abstract class TraceService : BuildService<TraceService.Parameters>,
    org.gradle.tooling.events.OperationCompletionListener {

    private val tag = "TraceServicePlugin-> "

    private var projectsEvaluated = false

    private val stringBuilder = StringBuilder()

    @Suppress("SimpleDateFormat")
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd :hh:mm:ss")

    private var startTime = 0L

    interface Parameters : BuildServiceParameters {
    }

    fun getNoMoreThanDigits(number: Double): String {
        val format = DecimalFormat("0.##")
        format.roundingMode = RoundingMode.FLOOR
        return format.format(number)
    }

    /**
     * 进入Task Execution阶段
     */
    fun onProjectsEvaluated() {
        println("$tag onProjectsEvaluated()")
        projectsEvaluated = true
    }

    /**
     * 每个task 任务的回调
     */
    override fun onFinish(event: FinishEvent) {
        if (!projectsEvaluated) {
//            ${event.result.startTime}  - ${event.result.endTime}
//            println("$tag from the cache! ")
            if (startTime == 0L) {
                startTime = event.result.startTime
                stringBuilder.append("\n")
                stringBuilder.append("构建开始时间：" + dateFormat.format(Date(startTime)) + "\n")
            }
        }
        if (event.result is FailureResult) {
            // task failed
        } else {
            // task succeeded OR skipped (and possibly other states, I'm not sure)
            if (event.displayName.contains(":assemble")) {
                stringBuilder.append("构建结束时间：" + dateFormat.format(Calendar.getInstance().time) + "\n")
                val totalTime = (System.currentTimeMillis() - startTime)
                stringBuilder.append("构建总耗时：" + totalTime + "ms")
                println("$tag  $stringBuilder = ${getNoMoreThanDigits(totalTime / 1000.00)}s")
            }
        }
    }
}

open class TraceServicePlugin @Inject constructor(
    private val buildEventsListenerRegistry: BuildEventsListenerRegistry
) : Plugin<Settings> {

    override fun apply(target: Settings) {
        target.gradle.run {
            val service = sharedServices.registerIfAbsent("traceService", TraceService::class) {
                parameters {
                    // configure service here
                }
            }
            buildEventsListenerRegistry.onTaskCompletion(service)
            projectsEvaluated {
                service.get().onProjectsEvaluated()
            }
        }
    }
}

//apply<TraceServicePlugin>()
