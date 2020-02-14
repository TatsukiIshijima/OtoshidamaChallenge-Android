package com.io.tatsuki.otoshidamachallenge

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class CameraViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private val TESS_DATA_DIR = "tessdata" + File.separator
        private const val TESS_TRAINED_DATA = "eng.traineddata"
    }

    private lateinit var textAnalyzer: TextAnalyzer

    private val _isReadyEvent = MutableLiveData<Event<Boolean>>()
    val isReadyEvent: LiveData<Event<Boolean>> = _isReadyEvent

    private val _resultText = MutableLiveData<String>()
    val resultText: LiveData<String> = _resultText

    fun checkTrainedData(context: Context) {
        val dataPath = context.filesDir.toString() + File.separator + TESS_DATA_DIR
        val dir = File(dataPath)
        if (!dir.exists() && dir.mkdirs()) {
            copyFiles(context)
        }
        if (dir.exists()) {
            val dataFilePath = dataPath + TESS_TRAINED_DATA
            val datafile = File(dataFilePath)
            if (!datafile.exists()) {
                copyFiles(context)
            }
        }
        _isReadyEvent.value = Event(true)
    }

    private fun copyFiles(context: Context) {
        try {
            val filePath =
                context.filesDir.toString() +
                        File.separator +
                        TESS_DATA_DIR +
                        TESS_TRAINED_DATA

            context.assets
                .open(TESS_DATA_DIR + TESS_TRAINED_DATA)
                .use { inputStream ->
                    FileOutputStream(filePath)
                        .use { outStream ->
                            val buffer = ByteArray(1024)
                            var read = inputStream.read(buffer)
                            while (read != -1) {
                                outStream.write(buffer, 0, read)
                                read = inputStream.read(buffer)
                            }
                            outStream.flush()
                        }

                    val file = File(filePath)

                    if (!file.exists()) throw FileNotFoundException()
                }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun initTextAnalyzer(context: Context,
                         widthCropPercent: Int,
                         heightCropPercent: Int): TextAnalyzer {
        textAnalyzer = TextAnalyzer(
            _resultText,
            context,
            widthCropPercent,
            heightCropPercent
        )
        return textAnalyzer
    }

    fun releaseAnalyzer() {
        textAnalyzer.end()
    }
}
