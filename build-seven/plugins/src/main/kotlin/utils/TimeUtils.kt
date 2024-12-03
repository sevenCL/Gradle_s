package utils

import java.math.RoundingMode
import java.text.DecimalFormat

/**
 * @author seven
 * @date 2024/8/28
 * @desc
 **/
object TimeUtils {
    fun getNoMoreThanDigits(number: Double): String {
        val format = DecimalFormat("0.##")
        format.roundingMode = RoundingMode.FLOOR
        return format.format(number)
    }
}