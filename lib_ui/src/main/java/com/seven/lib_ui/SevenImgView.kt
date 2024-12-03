package com.seven.lib_ui

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

/**
 * @author seven
 * @date 2024/12/2
 * @desc
 **/
class SevenImgView(context: Context, attrs: AttributeSet?) : AppCompatImageView(context, attrs) {

    init {
        setBackgroundColor(Color.RED)
        setPadding(12,12,12,12)
    }

}