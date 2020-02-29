package com.io.tatsuki.otoshidamachallenge

import android.graphics.*
import android.media.Image
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.text.FirebaseVisionText
import java.io.ByteArrayOutputStream

class TextAnalyzer(
    private val classNumberResult: MutableLiveData<String>,
    private val lotteryNumberResult: MutableLiveData<String>,
    private val widthCropPercent: Int,
    private val heightCropPercent: Int
) : ImageAnalysis.Analyzer {

    companion object {
        private val TAG = TextAnalyzer::class.java.simpleName
    }

    private val detector = FirebaseVision.getInstance().onDeviceTextRecognizer
    private var classNumberAnalyzerIsBusy = false
    private var lotteryNumberAnalyzerIsBusy = false

    override fun analyze(imageProxy: ImageProxy, rotationDegrees: Int) {
        val mediaImage = imageProxy.image
        if (mediaImage != null && !classNumberAnalyzerIsBusy && !lotteryNumberAnalyzerIsBusy) {
            classNumberAnalyzerIsBusy = true
            lotteryNumberAnalyzerIsBusy = true
            val imageRotation = degreesToFirebaseRotation(rotationDegrees)
            val image = FirebaseVisionImage.fromMediaImage(mediaImage, imageRotation)
            val bitmap = image.bitmap
            val croppedWidth = (bitmap.width * (1 - widthCropPercent / 100f)).toInt()
            val croppedHeight = (bitmap.height * (1 - heightCropPercent / 100f)).toInt()
            val x1 = ((bitmap.width - croppedWidth) / 2) - (bitmap.width / 4)
            val x2 = ((bitmap.width - croppedWidth) / 2) + (bitmap.width / 4)
            val y = (bitmap.height - croppedHeight) / 2
            val classNumberCropBmp = Bitmap.createBitmap(bitmap, x1, y, croppedWidth, croppedHeight)
            val lotteryNumberCropBmp = Bitmap.createBitmap(bitmap, x2, y, croppedWidth, croppedHeight)
            recognizeTextOnDevice(FirebaseVisionImage.fromBitmap(classNumberCropBmp), classNumberResult)
                .addOnCompleteListener { classNumberAnalyzerIsBusy = false }
            recognizeTextOnDevice(FirebaseVisionImage.fromBitmap(lotteryNumberCropBmp), lotteryNumberResult)
                .addOnCompleteListener { lotteryNumberAnalyzerIsBusy = false }

        }
    }

    private fun recognizeTextOnDevice(
        image: FirebaseVisionImage,
        result: MutableLiveData<String>
    ): Task<FirebaseVisionText> {
        return detector.processImage(image)
            .addOnSuccessListener { firebaseVisionText ->
                // Task completed successfully
                result.value = firebaseVisionText.text
                //Log.d(TAG, "${firebaseVisionText.text}")
            }
            .addOnFailureListener { exception ->
                // Task failed with an exception
                exception.message?.let {
                    Log.e(TAG, it)
                }
            }
    }

    private fun degreesToFirebaseRotation(degrees: Int): Int = when(degrees) {
        0 -> FirebaseVisionImageMetadata.ROTATION_0
        90 -> FirebaseVisionImageMetadata.ROTATION_90
        180 -> FirebaseVisionImageMetadata.ROTATION_180
        270 -> FirebaseVisionImageMetadata.ROTATION_270
        else -> throw Exception("Rotation must be 0, 90, 180, or 270.")
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