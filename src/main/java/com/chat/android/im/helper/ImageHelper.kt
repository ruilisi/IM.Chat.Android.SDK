package com.chat.android.im.helper

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.text.TextUtils
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.chat.android.im.R
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
import java.io.File

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

    private fun saveImage(context: Context): Boolean {
        if (!hasWriteExternalStoragePermission(context)) {
            checkWritingPermission(context)
            return false
        }
        if (ImagePipelineFactory.getInstance().mainFileCache.hasKey(cacheKey)) {
            val resource = ImagePipelineFactory.getInstance().mainFileCache.getResource(cacheKey)
            val cachedFile = (resource as FileBinaryResource).file
            val imageFormat = ImageFormatChecker.getImageFormat(resource.openStream())
            val imageDir = "${Environment.DIRECTORY_PICTURES}/IM.Chat Images/"
            val imagePath = Environment.getExternalStoragePublicDirectory(imageDir)
            val imageFile =
                    File(imagePath, "${cachedFile.nameWithoutExtension}.${imageFormat.fileExtension}")
            imagePath.mkdirs()
            imageFile.createNewFile()
            try {
                cachedFile.copyTo(imageFile, true)
                MediaScannerConnection.scanFile(
                        context,
                        arrayOf(imageFile.absolutePath),
                        null
                ) { path, uri ->
                    LogUtils.d(msg = "Scanned $path:")
                    LogUtils.d(msg = "-> uri=$uri")
                }
            } catch (ex: Exception) {
                val message = context.getString(R.string.msg_image_saved_failed)
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            } finally {
                val message = context.getString(R.string.msg_image_saved_successfully)
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
        return true
    }
}