package com.chat.android.im.utils

import android.app.Activity
import android.graphics.Bitmap
import android.os.Environment
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDateTime
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import kotlin.jvm.Throws

/**
 * Created by Ryan on 2020/11/6.
 */
@Throws(IOException::class)
fun Activity.createImageFile(): File {
    return File.createTempFile(
            "${LocalDateTime.now()}_",
            ".png",
            getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    )
}

/**
 * Returns a [ByteArray] of a [Bitmap].
 *
 * @param mimeType The MIME type of the [Bitmap].
 * @param quality The quality of the [Bitmap] for the resulting [ByteArray].
 * @param maxFileSizeAllowed The max file size allowed by the server. Note: The [quality] will be
 * decreased minus 10 until the [ByteArray] size fits the [maxFileSizeAllowed] value.
 * @return A [ByteArray] of a [Bitmap]
 */
suspend fun Bitmap.getByteArray(
        mimeType: String,
        quality: Int,
        maxFileSizeAllowed: Int
): ByteArray {
    lateinit var byteArray: ByteArray

    compressImageAndGetByteArray(mimeType, quality)?.let {
        if (it.size > maxFileSizeAllowed && maxFileSizeAllowed !in -1..0) {
            getByteArray(mimeType, quality - 10, maxFileSizeAllowed)
        } else {
            byteArray = it
        }
    }

    return byteArray
}

/**
 * Compress a [Bitmap] image.
 *
 * @param mimeType The MimeType of what the compressed image should be.
 * @return An [ByteArray] of a compressed image, otherwise null if the compression couldn't be done.
 */
suspend fun Bitmap.compressImageAndGetByteArray(mimeType: String, quality: Int = 100): ByteArray? {
    var byteArray: ByteArray? = null

    withContext(Dispatchers.Default) {
        val byteArrayOutputStream = ByteArrayOutputStream()
        val isCompressed = compress(mimeType.getCompressFormat(), quality, byteArrayOutputStream)
        if (isCompressed) {
            byteArray = byteArrayOutputStream.toByteArray()
        }
    }

    return byteArray
}

/**
 * Gets the [Bitmap.CompressFormat] based on the image MimeType.
 * Note: Supported formats are: PNG, JPEG and WEBP.
 */
fun String.getCompressFormat(): Bitmap.CompressFormat {
    return when {
        this.contains("jpeg") -> Bitmap.CompressFormat.JPEG
        this.contains("webp") -> Bitmap.CompressFormat.WEBP
        else -> Bitmap.CompressFormat.PNG
    }
}