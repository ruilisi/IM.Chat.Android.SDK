package com.chat.android.im.utils

import android.content.Context
import androidx.core.content.ContextCompat
import com.chat.android.im.config.RLS

fun getDrawableByName(context: Context, name: String): Int {
    return context.resources.getIdentifier(name, "drawable", context.packageName)
}

/**
 * 将dp值转换为px值
 *
 * @param dipValue dp值
 * @return px
 */
fun dip2px(context: Context, dipValue: Float): Int {
    return try {
        val scale = context.resources.displayMetrics.density
        (dipValue * scale + 0.5f).toInt()
    } catch (e: java.lang.Exception) {
        dipValue.toInt()
    }
}

/**
 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
 */
fun px2dip(context: Context, pxValue: Float): Float {
    val scale = context.resources.displayMetrics.density
    return pxValue / scale + 0.5f
}

fun isIMNull(): Boolean {
    if (RLS.getInstance().getDataConfig().base.isNullOrEmpty()
            || RLS.getInstance().getDataConfig().rid.isNullOrEmpty()
            || RLS.getInstance().getDataConfig().id.isNullOrEmpty()
            || RLS.getInstance().getDataConfig().token.isNullOrEmpty()) {
        return true
    }
    return false
}

/**
 * 将px值转换为sp值，保证文字大小不变
 *
 * @param pxValue
 * @param fontScale
 * （DisplayMetrics类中属性scaledDensity）
 * @return
 */
fun px2sp(context: Context, pxValue: Float): Int {
    val fontScale = context.resources.displayMetrics.scaledDensity
    return (pxValue / fontScale + 0.5f).toInt()
}

/**
 * 将sp值转换为px值，保证文字大小不变
 *
 * @param spValue
 * @param fontScale
 * （DisplayMetrics类中属性scaledDensity）
 * @return
 */
fun sp2px(context: Context, spValue: Float): Int {
    val fontScale = context.resources.displayMetrics.scaledDensity
    return (spValue * fontScale + 0.5f).toInt()
}

fun parseColor(context: Context, color: Int): Int {
    return if (color < 0) {
        color
    } else {
        ContextCompat.getColor(context, color)
    }
}