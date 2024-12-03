package bean

/**
 * @author seven
 * @date 2024/8/28
 * @desc
 **/
open class SevenBean(
    var openLog: Boolean = false,
    var excludeModule: Set<String> = HashSet(),
    var dexMergeIncremental: Boolean = true,
)