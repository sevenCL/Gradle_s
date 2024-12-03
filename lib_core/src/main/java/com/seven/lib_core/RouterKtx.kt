package com.seven.lib_core

import android.os.Bundle
import com.therouter.TheRouter
import com.therouter.router.interceptor.NavigationCallback

/**
 * @author seven
 * @date 2024/9/19
 * @desc
 **/
fun String.route(callback: NavigationCallback? = null, extras: Bundle.() -> Unit = {}) {
    TheRouter.build(this)
        .fillParams(extras)
        .navigation(callback = callback)
}