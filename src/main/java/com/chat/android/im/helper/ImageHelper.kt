package com.chat.android.im.helper

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import com.chat.android.im.R
import com.chat.android.im.helper.AndroidPermissionsHelper.WRITE_EXTERNAL_STORAGE_CODE_IMAGE
import com.chat.android.im.helper.AndroidPermissionsHelper.checkWritingPermission
import com.chat.android.im.helper.AndroidPermissionsHelper.hasWriteExternalStoragePermission
import com.chat.android.im.utils.LogUtils
import com.facebook.binaryresource.FileBinaryResource
import com.facebook.cache.common.CacheKey
import com.facebook.imageformat.ImageFormatChecker
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory
import com.facebook.imagepipeline.core.ImagePipelineFactory
import com.facebook.imagepipeline.request.ImageRequest
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.google.android.material.appbar.AppBarLayout
import com.stfalcon.frescoimageviewer.ImageViewer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.OutputStream

object ImageHelper {
    private var cacheKey: CacheKey? = null
    fun openImage(context: Context, imageUrl: String, imageName: String? = "") {
        var imageViewer: ImageViewer? = null
        val request =
                ImageRequestBuilder.newBuilderWithSource(Uri.parse(imageUrl))
                        .setLowestPermittedRequestLevel(ImageRequest.RequestLevel.DISK_CACHE)
                        .build()

        cacheKey = DefaultCacheKeyFactory.getInstance()
                .getEncodedCacheKey(request, null)
//        val pad = context.resources
//                .getDimensionPixelSize(R.dimen.viewer_toolbar_padding)
        val pad = context.resources
                .getDimensionPixelSize(R.dimen.dp_16)

        val lparams = AppBarLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val toolbar = Toolbar(context).also {
            it.inflateMenu(R.menu.image_actions)
            it.setOnMenuItemClickListener { view ->
                return@setOnMenuItemClickListener when (view.itemId) {
                    R.id.action_save_image -> saveImage(context)
                    else -> true
                }
            }
//            val titleSize = context.resources
//                    .getDimensionPixelSize(R.dimen.viewer_toolbar_title)
            val titleSize = context.resources
                    .getDimensionPixelSize(R.dimen.sp_16)
            val titleTextView = TextView(context).also { tv ->
                with(tv) {
                    text = imageName
                    setTextColor(Color.WHITE)
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, titleSize.toFloat())
                    ellipsize = TextUtils.TruncateAt.END
                    setSingleLine()
                    typeface = Typeface.DEFAULT_BOLD
                    setPadding(pad, pad, pad, pad)
                }
            }

            val backArrowView = ImageView(context).also { imgView ->
                with(imgView) {
                    setImageResource(R.drawable.ic_arrow_back_white_24dp)
                    setOnClickListener { imageViewer?.onDismiss() }
                    setPadding(0, pad, pad, pad)
                }
            }

            val layoutParams = AppBarLayout.LayoutParams(
                    AppBarLayout.LayoutParams.WRAP_CONTENT,
                    AppBarLayout.LayoutParams.WRAP_CONTENT
            )

            it.addView(backArrowView, layoutParams)
            it.addView(titleTextView, layoutParams)
        }

        val appBarLayout = AppBarLayout(context).also {
            with(it) {
                layoutParams = lparams
                setBackgroundColor(Color.BLACK)
                addView(
                        toolbar, AppBarLayout.LayoutParams(
                        AppBarLayout.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
                )
            }
        }

        val builder = ImageViewer.createPipelineDraweeControllerBuilder()
                .setImageRequest(request)
                .setAutoPlayAnimations(true)

        imageViewer = ImageViewer.Builder(context, listOf(imageUrl))
                .setOverlayView(appBarLayout)
                .setStartPosition(0)
                .hideStatusBar(false)
                .setCustomDraweeControllerBuilder(builder)
                .show()
    }

    fun saveImage(context: Context): Boolean {
        if (!hasWriteExternalStoragePermission(context)) {
            checkWritingPermission(context, WRITE_EXTERNAL_STORAGE_CODE_IMAGE)
            return false
        }
        var message = context.getString(R.string.msg_image_saved_successfully)
        if (ImagePipelineFactory.getInstance().mainFileCache.hasKey(cacheKey)) {
            try {
                //1.create path
                val resource = ImagePipelineFactory.getInstance().mainFileCache.getResource(cacheKey)
                val cachedFile = (resource as FileBinaryResource).file
                val imageFormat = ImageFormatChecker.getImageFormat(resource.openStream())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val dirPath = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!.absolutePath
                    val dirFile = File(dirPath)
                    if (!dirFile.exists()) dirFile.mkdirs()
                    val imageFile = File(dirPath, "${cachedFile.nameWithoutExtension}.${imageFormat.fileExtension}")
                    //2.save file
                    cachedFile.copyTo(imageFile, true)
                    //3.notify
                    MediaScannerConnection.scanFile(
                            context,
                            arrayOf(imageFile.absolutePath),
                            null
                    ) { path, uri ->
                        LogUtils.d(msg = "Scanned $path:")
                        LogUtils.d(msg = "-> uri=$uri")
                        moveImageToCamera(context, path, imageFile.name, "image/${imageFormat.fileExtension}")
                    }
                } else {
                    val imageDir = "${Environment.DIRECTORY_PICTURES}/IM.Chat Images/"
                    val imagePath = Environment.getExternalStoragePublicDirectory(imageDir)
                    val imageFile =
                            File(imagePath, "${cachedFile.nameWithoutExtension}.${imageFormat.fileExtension}")
                    imagePath.mkdirs()
                    imageFile.createNewFile()
                    cachedFile.copyTo(imageFile, true)
                    MediaScannerConnection.scanFile(
                            context,
                            arrayOf(imageFile.absolutePath),
                            null
                    ) { path, uri ->
                        LogUtils.d(msg = "Scanned $path:")
                        LogUtils.d(msg = "-> uri=$uri")
                    }
                }
            } catch (ex: Exception) {
                message = context.getString(R.string.msg_image_saved_failed)
            } finally {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
        return true
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun moveImageToCamera(context: Context, path: String?, name: String, type: String) {
        if (path != null) {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.DISPLAY_NAME, name)
            values.put(MediaStore.Images.Media.MIME_TYPE, type)
            values.put(MediaStore.Images.Media.TITLE, name)
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)//"DCIM/Camera"
            val external: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val resolver: ContentResolver = context.contentResolver
            val insertUri: Uri? = resolver.insert(external, values)

            if (insertUri != null) {
                File(path).runCatching {
                    inputStream().use { inputStream ->
                        resolver.openOutputStream(insertUri).use { outputStream ->
                            if (outputStream != null) {
                                inputStream.copyTo(outputStream)
                            }
                        }
                    }
                }
            }

        }
    }
}