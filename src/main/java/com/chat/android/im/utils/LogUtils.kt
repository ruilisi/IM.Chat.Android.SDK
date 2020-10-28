package com.chat.android.im.utils

import android.util.Log
import com.chat.android.im.BuildConfig

/**
 * Created by Ryan on 2020/9/3.
 */
object LogUtils {

    fun e(tag: String = "LogUtils", msg: String) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, msg)
        }
    }

    fun d(tag: String = "LogUtils", msg: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, msg)
        }
    }

}