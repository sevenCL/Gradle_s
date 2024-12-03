package utils

import java.util.Locale

/**
 * @author seven
 * @date 2024/9/21
 * @desc
 **/
fun String.replaceFirstChar(): String {
    return this.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(
            Locale.ROOT
        ) else it.toString()
    }
}