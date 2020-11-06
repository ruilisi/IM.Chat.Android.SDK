package com.chat.android.im.utils

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import androidx.core.content.ContextCompat
import com.chat.android.im.bean.MessageHistoryResultBody
import com.chat.android.im.bean.MsgType
import com.chat.android.im.config.RLS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

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

fun attachmentUrl(url: String?): String? {
    if (url.isNullOrEmpty()) return null
    if (url.startsWith("http")) return url

    val fullUrl = "${RLS.getInstance().getDataConfig().base.replace("wss", "https").replace("/websocket", "")}$url"
    val httpUrl = fullUrl.toHttpUrlOrNull()
    httpUrl?.let {
        return it.newBuilder().apply {
            addQueryParameter("rc_uid", RLS.getInstance().getDataConfig().id)
            addQueryParameter("rc_token", RLS.getInstance().getDataConfig().token)
        }.build().toString()
    }

    // Fallback to baseUrl + url
    return fullUrl
}

fun attachmentTitle(title: String?, vararg url: String?): CharSequence {
    title?.let { return it }

    url.filterNotNull().forEach {
        val fileUrl = it.toHttpUrlOrNull()
        fileUrl?.let { httpUrl ->
            return httpUrl.pathSegments.last()
        }
    }

    return ""
}

fun parseMsgType(data: MessageHistoryResultBody): MsgType {
    if (data.file?.type?.startsWith("image") == true) {
        return MsgType.IMAGE
    } else if (data.file?.type?.startsWith("video") == true) {
        return MsgType.VIDEO
    }
    return MsgType.TEXT
}


fun retrieveVideoFrameFromVideo(path: String?, thumb: ((bitmap: Bitmap?) -> Unit)) {
    path?.let {
        GlobalScope.launch(Dispatchers.IO) {
            var mediaMetadataRetriever: MediaMetadataRetriever? = null
            try {
                mediaMetadataRetriever = MediaMetadataRetriever()
                mediaMetadataRetriever.setDataSource(path, HashMap<String, String>())
                thumb(mediaMetadataRetriever.frameAtTime)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                mediaMetadataRetriever?.release()
            }
        }
    }
}
