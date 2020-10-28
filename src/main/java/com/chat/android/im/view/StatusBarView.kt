package com.chat.android.im.view

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View

/**
 * Created by Ryan on 2020/6/23.
 */
class StatusBarView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), mStatusBarHeight)
    }

    companion object {
        private var mStatusBarHeight = 0

        //此处代码可以放到StatusBarUtils
        fun getStatusBarHeight(context: Context): Int {
            if (mStatusBarHeight == 0) {
                val res = context.resources
                val resourceId = res.getIdentifier("status_bar_height", "dimen", "android")
                if (resourceId > 0) {
                    mStatusBarHeight = res.getDimensionPixelSize(resourceId)
                }
            }
            return mStatusBarHeight
        }
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mStatusBarHeight = getStatusBarHeight(context)
        }
    }
}