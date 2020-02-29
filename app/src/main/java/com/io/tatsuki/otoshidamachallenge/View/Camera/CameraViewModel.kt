package com.io.tatsuki.otoshidamachallenge.View.Camera

import androidx.lifecycle.*

class CameraViewModel : ViewModel() {

    val classNumberAnalyzeResult = MutableLiveData<String>()
    val lotteryNumberAnalyzeResult = MutableLiveData<String>()

    private val _combinedAnalyzeResult = MediatorLiveData<String>()
    val combinedAnalyzeResult: LiveData<String> = _combinedAnalyzeResult

    init {
        val analyzeObserver = Observer<String> {
            val classNumberText = classNumberAnalyzeResult.value ?: ""
            val lotteryNumberText = lotteryNumberAnalyzeResult.value ?: ""
            if (classNumberText.isNotEmpty() && lotteryNumberText.isNotEmpty()) {
                _combinedAnalyzeResult.value = "${classNumberText}${lotteryNumberText}"
            }
        }
        _combinedAnalyzeResult.addSource(classNumberAnalyzeResult, analyzeObserver)
        _combinedAnalyzeResult.addSource(lotteryNumberAnalyzeResult, analyzeObserver)
    }
}
