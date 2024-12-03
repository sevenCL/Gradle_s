package com.seven.lib_common

import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.Glide
import com.seven.lib_core.BaseApplication.Companion.instance


/**
 * @author seven
 * @date 2024/9/19
 * @desc
 **/

fun AppCompatImageView.load(url: String) {
    Glide.with(instance)
        .load(url)
        .centerCrop()
        .into(this)
}