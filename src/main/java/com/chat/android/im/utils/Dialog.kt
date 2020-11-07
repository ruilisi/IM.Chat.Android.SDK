package com.chat.android.im.utils

import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.chat.android.im.R
import com.chat.android.im.activity.ChatActivity
import com.chat.android.im.config.RLS


/**
 * Created by Ryan on 2020/11/6.
 */
fun ChatActivity.showFileAttachmentDialog(uri: Uri) {

    val dialogView = View.inflate(this, R.layout.file_attachments_dialog, null)
    val alertDialog = AlertDialog.Builder(this).setView(dialogView).create()
    val imagePreview = dialogView.findViewById<ImageView>(R.id.image_preview)
    val sendButton = dialogView.findViewById<android.widget.Button>(R.id.button_send)
    val cancelButton = dialogView.findViewById<android.widget.Button>(R.id.button_cancel)
    val description = dialogView.findViewById<EditText>(R.id.text_file_description)
    val audioVideoAttachment = dialogView.findViewById<FrameLayout>(R.id.audio_video_attachment)
    val textFile = dialogView.findViewById<TextView>(R.id.text_file_name)

    imagePreview.isVisible = false
    audioVideoAttachment.isVisible = false
    textFile.isVisible = false

    lateinit var mimeType: String
    var bitmap: Bitmap? = null

    uri.getMimeType(this)?.let {
        mimeType = it
        description.text.clear()
        when {
            mimeType.startsWith("image") -> {
                if (mimeType.contains("gif")) {
                    Glide
                            .with(this)
                            .asGif()
                            .load(uri)
                            .apply(RequestOptions().fitCenter())
                            .into(imagePreview)
                } else {
                    Glide
                            .with(this)
                            .asBitmap()
                            .load(uri)
                            .apply(RequestOptions().fitCenter())
                            .into(object : SimpleTarget<Bitmap>() {
                                override fun onResourceReady(
                                        resource: Bitmap,
                                        transition: Transition<in Bitmap>?
                                ) {
                                    bitmap = resource
                                    imagePreview.setImageBitmap(resource)
                                }
                            })
                }
                imagePreview.isVisible = true
            }
            mimeType.startsWith("video") -> audioVideoAttachment.isVisible = true
            else -> {
                textFile.isVisible = true
                textFile.text = uri.getFileName(this)
            }
        }
    }

    sendButton.setOnClickListener {
        bitmap?.let { bitmap ->
            uploadImage(
                    RLS.getInstance().getDataConfig().rid,
                    mimeType,
                    uri,
                    bitmap,
                    (citation ?: "") + description.text.toString()
            )
        }.ifNull {
            uploadFile(
                    RLS.getInstance().getDataConfig().rid,
                    mimeType,
                    uri,
                    (citation ?: "") + description.text.toString()
            )
        }
        alertDialog.dismiss()
    }
    cancelButton.setOnClickListener { alertDialog.dismiss() }
    alertDialog.show()
}