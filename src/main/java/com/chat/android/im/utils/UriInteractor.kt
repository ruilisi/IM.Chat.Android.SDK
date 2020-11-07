package com.chat.android.im.utils

import android.content.Context
import android.net.Uri

class UriInteractor constructor(private val context: Context) {

    /**
     * Returns the file name from the [Uri].
     */
    fun getFileName(uri: Uri): String? = uri.getFileName(context)

    /**
     * Returns the MimeType from the [Uri].
     */
    fun getMimeType(uri: Uri): String = uri.getMimeType(context) ?: ".png"

    /**
     * Returns the file size from the [Uri].
     */
    fun getFileSize(uri: Uri) = uri.getFileSize(context)

    /**
     * Returns the InputStream from the [Uri].
     */
    fun getInputStream(uri: Uri) = uri.getInputStream(context)

    /**
     * Returns the Bitmap from the [Uri].
     *
     * Note: It should be an image.
     */
    fun getBitmap(uri: Uri) = uri.getBitmpap(context)
}