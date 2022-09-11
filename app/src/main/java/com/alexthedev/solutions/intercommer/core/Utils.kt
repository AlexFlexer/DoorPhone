package com.alexthedev.solutions.intercommer.core

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


fun saveInPicturesDirectory(
    context: Context,
    bitmap: Bitmap,
    onSuccess: (uri: Uri) -> Unit,
    onFailure: () -> Unit
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
        saveLegacy(context, bitmap, onSuccess, onFailure)
    else
        saveModern(context, bitmap, onSuccess, onFailure)
}

@RequiresApi(Build.VERSION_CODES.Q)
private fun saveModern(
    context: Context,
    bitmap: Bitmap,
    onSuccess: (uri: Uri) -> Unit,
    onFailure: () -> Unit
) {
    //Generating a file name
    val filename = "${System.currentTimeMillis()}.jpg"

    //Output stream
    var fos: OutputStream? = null
    var imageUri: Uri? = null

    context.contentResolver?.also { resolver ->

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        fos = imageUri?.let { resolver.openOutputStream(it) }
    }

    if (fos == null || imageUri == null) {
        onFailure()
    } else {
        fos?.use {
            try {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                onSuccess(imageUri!!)
            } catch (t: Throwable) {
                onFailure()
            }
        }
    }
}

@Suppress("deprecation")
private fun saveLegacy(
    context: Context,
    bitmap: Bitmap,
    onSuccess: (uri: Uri) -> Unit,
    onFailure: () -> Unit
) {
    val fileName = "${System.currentTimeMillis()}.png"
    val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
    val file = File(picturesDir, fileName)
    if (file.createNewFile()) {
        var stream: FileOutputStream? = null
        try {
            stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()
            onSuccess(
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
            )
        } catch (e: Exception) {
            onFailure()
            e.printStackTrace()
        } finally {
            stream?.close()
        }
    } else onFailure()
}