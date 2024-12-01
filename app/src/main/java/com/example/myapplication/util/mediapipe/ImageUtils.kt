package com.example.myapplication.util.mediapipe

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.YuvImage
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream

fun imageToNV21(image: ImageProxy): ByteArray {
    val planes = image.planes
    val yPlane = planes[0]
    val uPlane = planes[1]
    val vPlane = planes[2]
    val yBuffer = yPlane.buffer
    val uBuffer = uPlane.buffer
    val vBuffer = vPlane.buffer

    val numPixels = (image.width * image.height * 1.5).toInt()
    val nv21 = ByteArray(numPixels)

    val yRowStride = yPlane.rowStride
    val yPixelStride = yPlane.pixelStride
    val uvRowStride = uPlane.rowStride
    val uvPixelStride = uPlane.pixelStride

    var idY = 0
    var idUV = image.width * image.height
    val uvWidth = image.width / 2
    val uvHeight = image.height / 2

    for (y in 0 until image.height) {
        val yOffset = y * yRowStride
        val uvOffset = y * uvRowStride

        for (x in 0 until image.width) {
            nv21[idY++] = yBuffer[yOffset + x * yPixelStride]

            if (y < uvHeight && x < uvWidth) {
                val uvIndex = uvOffset + (x * uvPixelStride)
                nv21[idUV++] = vBuffer[uvIndex]
                nv21[idUV++] = uBuffer[uvIndex]
            }
        }
    }

    return nv21
}

fun nv21ToBitmap(nv21: ByteArray, width: Int, height: Int): Bitmap {
    val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(android.graphics.Rect(0, 0, width, height), 100, out)
    val imageBytes = out.toByteArray()
    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)!!
}