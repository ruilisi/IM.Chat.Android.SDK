package com.chat.android.im.helper

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.view.ContextThemeWrapper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

object AndroidPermissionsHelper {

    const val WRITE_EXTERNAL_STORAGE_CODE_IMAGE = 1
    const val WRITE_EXTERNAL_STORAGE_CODE_PHOTO = 2
    const val CAMERA_CODE = 2

    private fun checkPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
                context,
                permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission(context: Activity, permission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(context, arrayOf(permission), requestCode)
    }

    fun hasCameraPermission(context: Context): Boolean {
        return checkPermission(context, Manifest.permission.CAMERA)
    }

    fun getCameraPermission(activity: Activity) {
        requestPermission(activity, Manifest.permission.CAMERA, CAMERA_CODE)
    }

    fun hasWriteExternalStoragePermission(context: Context): Boolean {
        return checkPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    fun getWriteExternalStoragePermission(fragment: Fragment,requestCode: Int) {
        fragment.requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                requestCode
        )
    }

    fun checkWritingPermission(context: Context,requestCode: Int) {
        if (context is ContextThemeWrapper) {
            val activity = if (context.baseContext is Activity) context.baseContext as Activity else context as Activity
            requestPermission(
                    activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    requestCode
            )
        }
    }
}