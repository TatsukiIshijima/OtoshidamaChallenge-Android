package com.io.tatsuki.otoshidamachallenge

import android.content.Context
import android.graphics.*
import android.media.Image
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.lifecycle.MutableLiveData
import com.googlecode.tesseract.android.TessBaseAPI
import java.io.ByteArrayOutputStream

class TextAnalyzer(
    private val result: MutableLiveData<String>,
    context: Context,
    private val widthCropPercent: Int,
    private val heightCropPercent: Int
) : ImageAnalysis.Analyzer {

    companion object {
        private val TAG = TextAnalyzer::class.java.simpleName
    }

    private val baseApi = TessBaseAPI()

    init {
        baseApi.init(context.filesDir.toString(), "eng")
    }

    override fun analyze(imageProxy: ImageProxy, rotationDegrees: Int) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val bitmap = mediaImage.toBitmap()
            val croppedWidth = (bitmap.width * (1 - widthCropPercent / 100f)).toInt()
            val croppedHeight = (bitmap.height * (1 - heightCropPercent / 100f)).toInt()
            val x = (bitmap.width - croppedWidth) / 2
            val y = (bitmap.height - croppedHeight) / 2
            val cropBmp = Bitmap.createBitmap(bitmap, x, y, croppedWidth, croppedHeight)
            baseApi.setImage(cropBmp)
            result.postValue(baseApi.utF8Text)
        }
    }

    fun end() {
        baseApi.end()
    }
}

fun Image.toBitmap(): Bitmap {
    val yBuffer = planes[0].buffer // Y
    val uBuffer = planes[1].buffer // U
    val vBuffer = planes[2].buffer // V

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)

    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 50, out)
    val imageBytes = out.toByteArray()
    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}