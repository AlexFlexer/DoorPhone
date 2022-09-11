package com.alexthedev.solutions.intercommer.core

import android.graphics.*
import androidx.annotation.ColorInt


fun createCircularBitsImage(
    binaryStringToEncode: String,
    imageWidth: Int,
    @ColorInt color0: Int,
    @ColorInt color1: Int,
    antiAlias: Boolean
): Bitmap {
    if (binaryStringToEncode.isEmpty() || !binaryStringToEncode.isBinary())
        throw StringIsNotBinaryException(binaryStringToEncode)
    val bitmap = Bitmap.createBitmap(imageWidth, imageWidth, Bitmap.Config.ARGB_8888)
    val paint = Paint()
    paint.style = Paint.Style.FILL
    paint.isAntiAlias = antiAlias
    val canvas = Canvas(bitmap)
    val oval = setupOval(imageWidth)
    val anglePerSector = 360f / binaryStringToEncode.length
    binaryStringToEncode.toBooleanArray().forEachIndexed { index, b ->
        paint.color = if (b) color1 else color0
        canvas.drawArc(oval, anglePerSector * index, anglePerSector, true, paint)
    }
    return bitmap
}

private fun setupOval(imageWidth: Int): RectF {
    val result = RectF()
    val center = imageWidth / 2f
    val radius = imageWidth / 2f
    result.set(
        center - radius,
        center - radius,
        center + radius,
        center + radius
    )
    return result
}

class StringIsNotBinaryException(problemString: String) :
    Exception("The given string $problemString should contain either 0 or 1 only")