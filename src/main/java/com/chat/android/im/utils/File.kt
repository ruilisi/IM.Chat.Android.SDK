package com.chat.android.im.utils

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import com.chat.android.im.internal.AuthenticationErrorMessage
import com.chat.android.im.internal.ErrorMessage
import com.chat.android.im.BuildConfig
import com.chat.android.im.activity.ChatActivity
import com.chat.android.im.exception.*
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException
import java.io.InputStream
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Created by Ryan on 2020/11/6.
 */

fun ChatActivity.uploadImage(roomId: String, mimeType: String, uri: Uri, bitmap: Bitmap, msg: String) {
    launchUI(strategy) {
        showLoading()
        try {
            withContext(Dispatchers.Default) {
                val fileName = uriInteractor.getFileName(uri) ?: uri.toString()
                if (fileName.isEmpty()) {
                    showInvalidFileMessage()
                } else {
                    val byteArray =
                            bitmap.getByteArray(mimeType, 100, 1024 * 1024)
                    println("------------00000:${byteArray.size}")
                    retryIO("uploadFile($roomId, $fileName, $mimeType") {
                        uploadFile(
                                roomId,
                                fileName,
                                mimeType,
                                msg,
                                description = fileName
                        ) {
                            byteArray.inputStream()
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            LogUtils.d(msg = ex.message ?: "")
            when (ex) {
                is RocketChatException -> showMessage(ex)
                else -> showGenericErrorMessage()
            }
        } finally {
            hideLoading()
        }
    }
}

fun ChatActivity.uploadFile(roomId: String, mimeType: String, uri: Uri, msg: String) {
    launchUI(strategy) {
        showLoading()
        try {
            withContext(Dispatchers.Default) {
                val fileName = uriInteractor.getFileName(uri) ?: uri.toString()
                val fileSize = uriInteractor.getFileSize(uri)
                val maxFileSizeAllowed = Int.MAX_VALUE

                when {
                    fileName.isEmpty() -> showInvalidFileMessage()
                    fileSize > maxFileSizeAllowed && maxFileSizeAllowed !in -1..0 ->
                        showInvalidFileSize(fileSize, maxFileSizeAllowed)
                    else -> {
                        retryIO("uploadFile($roomId, $fileName, $mimeType") {
                            uploadFile(
                                    roomId,
                                    fileName,
                                    mimeType,
                                    msg,
                                    description = fileName
                            ) {
                                uriInteractor.getInputStream(uri)
                            }
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            when (ex) {
                is RocketChatException -> showMessage(ex)
                else -> showGenericErrorMessage()
            }
        } finally {
            hideLoading()
        }
    }
}


private suspend fun ChatActivity.uploadFile(
        roomId: String,
        fileName: String,
        mimeType: String,
        msg: String = "",
        description: String = "",
        inputStreamProvider: () -> InputStream?
) {
    withContext(Dispatchers.IO) {
        val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                        "file", fileName,
                        InputStreamRequestBody(mimeType.toMediaTypeOrNull(), inputStreamProvider)
                )
                .addFormDataPart("msg", msg)
                .addFormDataPart("description", description)
                .build()
        uploadFile(roomId, body)
    }
}

private suspend fun ChatActivity.uploadFile(roomId: String, body: RequestBody) {
    val httpUrl = requestUrl(restUrl().toHttpUrlOrNull()!!, "rooms.upload")
            .addPathSegment(roomId)
            .build()
    val request = requestBuilderForAuthenticatedMethods(httpUrl).post(body).build()
    handleRestCall<Any>(request, Any::class.java, largeFile = true)
}

private fun requestUrl(baseUrl: HttpUrl, method: String): HttpUrl.Builder {
    return baseUrl.newBuilder()
            .addPathSegment("api")
            .addPathSegment("v1")
            .addPathSegment(method)
}

private fun requestBuilderForAuthenticatedMethods(httpUrl: HttpUrl): Request.Builder {
    val builder = requestBuilder(httpUrl)
    builder.addHeader("X-Auth-Token", authToken()).addHeader("X-User-Id", userId())
    return builder
}

private fun requestBuilder(httpUrl: HttpUrl): Request.Builder =
        Request.Builder()
                .url(httpUrl)
                .header("User-Agent", "RC Mobile; Android ${Build.VERSION.RELEASE}; v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                .tag(Any())

private suspend fun <T> ChatActivity.handleRestCall(
        request: Request,
        type: Type,
        largeFile: Boolean = false,
        allowRedirects: Boolean = true
): T {
    val response = handleRequest(request, largeFile, allowRedirects)
    return handleResponse(response, type)
}

private suspend fun ChatActivity.handleRequest(
        request: Request,
        largeFile: Boolean = false,
        allowRedirects: Boolean = true
): Response = suspendCancellableCoroutine { continuation ->
    val callback = object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            LogUtils.d(msg = "Failed request: ${request.method} - ${request.url} - ${e.message}")
            continuation.tryResumeWithException {
                RocketChatNetworkErrorException("Network Error: ${e.message}", e, request.url.toString())
            }
        }

        override fun onResponse(call: Call, response: Response) {
            LogUtils.d(msg = "Successful HTTP request: ${request.method} - ${request.url}: ${response.code} ${response.message}")
            if (!response.isSuccessful) {
                continuation.tryResumeWithException {
                    processCallbackError(moshi, request, response, allowRedirects)
                }
            } else {
                continuation.tryToResume { response }
            }
        }
    }

    LogUtils.d(msg = "Enqueueing: ${request.method} - ${request.url}")

    val client = ensureClient(largeFile, allowRedirects)
    client.newCall(request).enqueue(callback)

    continuation.invokeOnCancellation {
        client.cancel(request.tag())
    }
}

private fun OkHttpClient.cancel(tag: Any?) {
    tag?.let {
        dispatcher.queuedCalls().filter { tag == it.request().tag() }.forEach { it.cancel() }
        dispatcher.runningCalls().filter { tag == it.request().tag() }.forEach { it.cancel() }
    }
}

private fun ChatActivity.ensureClient(largeFile: Boolean, allowRedirects: Boolean): OkHttpClient {
    return if (largeFile || !allowRedirects) {
        okHttpClient.newBuilder().apply {
            if (largeFile) {
                writeTimeout(90, TimeUnit.SECONDS)
                readTimeout(90, TimeUnit.SECONDS)
            }
            followRedirects(allowRedirects)
        }.build()
    } else {
        okHttpClient
    }
}

private fun processCallbackError(
        moshi: Moshi,
        request: Request,
        response: Response,
        allowRedirects: Boolean = true
): RocketChatException {
    var exception: RocketChatException
    try {
        if (response.isRedirect && !allowRedirects) {
            exception = RocketChatInvalidProtocolException("Invalid Protocol", url = request.url.toString())
        } else {
            val body = response.body?.string() ?: "missing body"
            LogUtils.d(msg = "Error body: $body")
            exception = if (response.code == 401) {
                val adapter: JsonAdapter<AuthenticationErrorMessage>? = moshi.adapter(AuthenticationErrorMessage::class.java)
                val message: AuthenticationErrorMessage? = adapter?.fromJson(body)
                if (message?.error?.contentEquals("totp-required") == true)
                    RocketChatTwoFactorException(message.message, request.url.toString())
                else
                    RocketChatAuthException(message?.message
                            ?: "Authentication problem", request.url.toString())
            } else {
                val adapter: JsonAdapter<ErrorMessage>? = moshi.adapter(ErrorMessage::class.java)
                val message = adapter?.fromJson(body)
                RocketChatApiException(message?.errorType
                        ?: response.code.toString(), message?.error
                        ?: "unknown error",
                        url = request.url.toString())
            }
        }
    } catch (e: Exception) {
        exception = RocketChatApiException(response.code.toString(), e.message!!, e, request.url.toString())
    } finally {
        response.body?.close()
    }

    return exception
}

private fun <T> ChatActivity.handleResponse(response: Response, type: Type): T {
    val url = response.priorResponse?.request?.url ?: response.request.url
    try {
        // Override nullability, if there is no adapter, moshi will throw...
        val adapter: JsonAdapter<T> = moshi.adapter(type)!!

        val source = response.body?.source()
        checkNotNull(source) { "Missing body" }

        return adapter.fromJson(source)
                ?: throw RocketChatInvalidResponseException("Error parsing JSON message", url = url.toString())
    } catch (ex: Exception) {
        when (ex) {
            is RocketChatException -> throw ex // already a RocketChatException, just rethrow it.
            else -> throw RocketChatInvalidResponseException(ex.message!!, ex, url.toString())
        }
    } finally {
        response.body?.close()
    }
}

private inline fun <T> CancellableContinuation<T>.tryToResume(getter: () -> T) {
    isActive || return
    try {
        resume(getter())
    } catch (exception: Throwable) {
        resumeWithException(exception)
    }
}

private inline fun <T> CancellableContinuation<T>.tryResumeWithException(getter: () -> Exception) {
    isActive || return
    resumeWithException(getter())
}
