package service

import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.tooling.events.FinishEvent
import org.gradle.tooling.events.OperationCompletionListener
import org.gradle.tooling.events.task.TaskFailureResult
import org.gradle.tooling.events.task.TaskFinishEvent
import org.gradle.tooling.events.task.TaskSuccessResult
import utils.ChangeUtils
import utils.LogUtils
import utils.TimeUtils
import java.text.SimpleDateFormat
import java.util.Calendar

/**
 * @author seven
 * @date 2024/9/20
 * @desc
 **/
abstract class BuildListenerService :
    BuildService<BuildListenerService.Params>, OperationCompletionListener, AutoCloseable {

    interface Params : BuildServiceParameters

    private var buildStartTime: Long = 0

    private val stringBuilder = StringBuilder()

    @Suppress("SimpleDateFormat")
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd :hh:mm:ss")


    init {
        buildStartTime = System.currentTimeMillis()
        stringBuilder.append("\n")
        stringBuilder.append("构建开始时间：" + dateFormat.format(Calendar.getInstance().time) + "\n")
    }

    override fun onFinish(event: FinishEvent) {
        if (event is TaskFinishEvent) {

        }
        if (event is TaskFailureResult) {

        }
        if (event is TaskSuccessResult){

        }
    }

    override fun close() {
        ChangeUtils.flushJsonFile()
        stringBuilder.append("构建结束时间：" + dateFormat.format(Calendar.getInstance().time) + "\n")
        val totalTime = (System.currentTimeMillis() - buildStartTime)
        stringBuilder.append("构建总耗时：" + totalTime + "ms")
        LogUtils.d("   $stringBuilder = ${TimeUtils.getNoMoreThanDigits(totalTime / 1000.00)}s")
    }

    fun test() {

    }
}