package com.chat.android.im.utils

import android.annotation.TargetApi
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import com.chat.android.im.R
import com.chat.android.im.bean.DeepLinkInfo
import java.io.*
import kotlin.jvm.Throws

/**
 * Created by Ryan on 2020/11/6.
 */
fun Uri.getFileName(context: Context): String? {
    val cursor = context.contentResolver.query(this, null, null, null, null, null)

    var fileName: String? = null
    cursor?.use {
        if (it.moveToFirst()) {
            fileName = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
        }
    }
    return fileName
}

fun Uri.getFileSize(context: Context): Int {
    var fileSize: String? = null
    if (scheme == ContentResolver.SCHEME_CONTENT) {
        try {
            val fileInputStream = context.contentResolver.openInputStream(this)
            fileSize = fileInputStream?.available().toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    } else if (scheme == ContentResolver.SCHEME_FILE) {
        val path = this.path
        try {
            val f = File(path)
            fileSize = f.length().toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return fileSize?.toIntOrNull() ?: -1
}

fun Uri.getMimeType(context: Context): String? {
    return if (scheme == ContentResolver.SCHEME_CONTENT) {
        context.contentResolver?.getType(this) ?: ""
    } else {
        val fileExtension = MimeTypeMap.getFileExtensionFromUrl(toString())
        if (fileExtension != null) {
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase())
        } else {
            "application/octet-stream"
        }
    }
}

@TargetApi(Build.VERSION_CODES.N)
fun Uri.isVirtualFile(context: Context): Boolean {
    if (!DocumentsContract.isDocumentUri(context, this)) {
        return false
    }

    val cursor = context.contentResolver.query(
            this,
            arrayOf(DocumentsContract.Document.COLUMN_FLAGS),
            null,
            null,
            null
    )

    var flags = 0
    cursor?.use {
        if (it.moveToFirst()) {
            flags = it.getInt(0)
        }
    }

    return flags and DocumentsContract.Document.FLAG_VIRTUAL_DOCUMENT != 0
}

@Throws(IOException::class)
fun Uri.getInputStreamForVirtualFile(context: Context, mimeTypeFilter: String): FileInputStream? {

    val resolver = context.contentResolver

    val openableMimeTypes = resolver.getStreamTypes(this, mimeTypeFilter)

    if (openableMimeTypes == null || openableMimeTypes.isEmpty()) {
        throw FileNotFoundException()
    }

    return resolver.openTypedAssetFileDescriptor(this, openableMimeTypes[0], null)
            ?.createInputStream()
}

fun Uri.getInputStream(context: Context): InputStream? {
    if (isVirtualFile(context)) {
        return getInputStreamForVirtualFile(context, "*/*")
    }

    return context.contentResolver.openInputStream(this)
}

fun Uri.getBitmpap(context: Context): Bitmap? {
    return MediaStore.Images.Media.getBitmap(context.contentResolver, this)
}

fun Uri.getDeepLinkInfo(context: Context): DeepLinkInfo? {
    return when {
        isAuthenticationDeepLink(context) -> {
            val host = getQueryParameter("host")
            val url = if (host?.startsWith("http") == true) host else "https://$host"
            val userId = getQueryParameter("userId")
            val token = getQueryParameter("token")
            try {
                DeepLinkInfo(url, userId, token, null, null, null)
            } catch (ex: Exception) {
                null
            }
        }
        isCustomSchemeRoomLink() -> {
            val hostValue = getQueryParameter("host")
            val url = if (hostValue?.startsWith("http") == true) hostValue else "https://$hostValue"
            val rid = getQueryParameter("rid")
            val pathValue = getQueryParameter("path")
            val pathSplit = pathValue?.split("/")
            val roomType = pathSplit?.get(0)
            val roomName = pathSplit?.get(1)
            try {
                DeepLinkInfo(url, null, null, rid, roomType, roomName)
            } catch (ex: Exception) {
                LogUtils.d(msg = "Error parsing custom scheme room link")
                null
            }
        }
        isWebSchemeRoomLink() -> {
            val url = "https://$host"
            val pathSplit = path?.split("/")
            val roomType = pathSplit?.get(1)
            val roomName = pathSplit?.get(2)
            try {
                DeepLinkInfo(url, null, null, null, roomType, roomName)
            } catch (ex: Exception) {
                LogUtils.d(msg = "Error parsing login deeplink")
                null
            }
        }
        else -> null
    }
}

fun Uri.isDynamicLink(activity: Activity): Boolean {
    return (host != null && host?.contains(activity.getString(R.string.dynamic_link_host_url)) == true)
}

// Authentication deep link defined here: https://rocket.chat/docs/developer-guides/deeplink/#authentication
fun Uri.isAuthenticationDeepLink(context: Context): Boolean {
    if (host == "auth")
        return true
    else if (host == context.getString(R.string.community_server_url) && path == "/auth")
        return true
    return false
}

// Custom scheme room deep link defined here: https://rocket.chat/docs/developer-guides/deeplink/#channel--group--dm
fun Uri.isCustomSchemeRoomLink(): Boolean {
    return (scheme?.startsWith("rocketchat")) == true && host == "room"
}

// http(s) scheme deep link not yet documented. Ex: https://open.rocket.chat/direct/testuser1
fun Uri.isWebSchemeRoomLink(): Boolean {
    val roomType = path?.split("/")?.get(1)
    return (scheme?.startsWith("http")) == true && (roomType == "channel" || roomType == "group" || roomType == "direct")
}