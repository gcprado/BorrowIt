package com.pigs.borrowit.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

object ImageUtils {
    suspend fun compressImage(
        context: Context,
        uri: Uri,
        maxWidth: Int = 1024,
        quality: Int = 80
    ): ByteArray? {
        return withContext(Dispatchers.IO) {
            try {
                @Suppress("DEPRECATION")
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                val scaledBitmap = if (bitmap.width > maxWidth) {
                    val scale = maxWidth.toFloat() / bitmap.width
                    val newHeight = (bitmap.height * scale).toInt()
                    Bitmap.createScaledBitmap(bitmap, maxWidth, newHeight, true)
                } else {
                    bitmap
                }
                val outputStream = ByteArrayOutputStream()
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                outputStream.toByteArray()
            } catch (e: Exception) {
                Log.e("ImageUtils", "Error compressing image", e)
                null
            }
        }
    }
}
