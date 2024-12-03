package com.seven.lib_core

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

/**
 * @author seven
 * @date 2024/9/19
 * @desc
 **/
abstract class BaseApplication : Application() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var instance: Context
    }

    override fun onCreate() {
        super.onCreate()
        instance = applicationContext
        init()
    }

    abstract fun init()
}