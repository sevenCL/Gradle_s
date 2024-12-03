package cache

/**
 * @author seven
 * @date 2024/8/28
 * @desc
 **/
abstract class LocalCache {

    abstract fun uploadLocalCache()

    companion object {
        // project name 对应 flat aar/jar name
        val localCacheMap = mutableMapOf<String, String>()
    }

    fun putIntoLocalCache(projectName: String, path: String) {
        localCacheMap[projectName] = path
    }
}