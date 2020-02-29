package com.io.tatsuki.otoshidamachallenge.View.Camera

import androidx.lifecycle.*
import com.io.tatsuki.otoshidamachallenge.MatchingRank
import com.io.tatsuki.otoshidamachallenge.Repository.LotteryNumbersRepository
import kotlinx.coroutines.launch

class CameraViewModel(
    private val repository: LotteryNumbersRepository
) : ViewModel() {

    val classNumberAnalyzeResult = MutableLiveData<String>()
    val lotteryNumberAnalyzeResult = MutableLiveData<String>()

    private val _combinedAnalyzeResult = MediatorLiveData<String>()
    val combinedAnalyzeResult: LiveData<String> = _combinedAnalyzeResult

    private val _matchingRankResult = MutableLiveData<MatchingRank>()
    val matchingRankResult: LiveData<MatchingRank> = _matchingRankResult

    private val _isCompletedGetLotteryNumbersData = MutableLiveData<Boolean>()
    val isCompletedGetLotteryNumbersData = _isCompletedGetLotteryNumbersData

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

    fun getLotteryNumbers() {
        viewModelScope.launch {
            repository.loadLotteryNumbers()
            _isCompletedGetLotteryNumbersData.value = true
        }
    }

    fun matchLotteryNumbers(analyzedText: String) {
        val savedLotteryNumbers =
            repository.cacheLotteryNumbers ?: throw NullPointerException("LotteryNumbers cache is null.")
        if (analyzedText.length <= 9) {
            _matchingRankResult.value = MatchingRank.NONE
            return
        }
        val specialClassPrimaryTarget = analyzedText.substring(analyzedText.length - 10)
        if ((specialClassPrimaryTarget == "${savedLotteryNumbers.specialPrimaryForward}${savedLotteryNumbers.specialPrimaryBackward}") ||
            (specialClassPrimaryTarget == "${savedLotteryNumbers.specialSecondaryForward}${savedLotteryNumbers.specialSecondaryBackward}") ||
            (specialClassPrimaryTarget == "${savedLotteryNumbers.specialTertiaryForward}${savedLotteryNumbers.specialTertiaryBackward}") ||
            (specialClassPrimaryTarget == "${savedLotteryNumbers.specialQuaternaryForward}${savedLotteryNumbers.specialQuaternaryBackward}")) {
            _matchingRankResult.value = MatchingRank.SPECIAL_PRIMARY
            return
        }

        val specialClassSecondaryTarget = analyzedText.substring(analyzedText.length - 7)
        if (specialClassSecondaryTarget == "${savedLotteryNumbers.specialQuinaryForward}${savedLotteryNumbers.specialQuinaryBackward}") {
            _matchingRankResult.value = MatchingRank.SPECIAL_SECONDARY
            return
        }

        val firstClassTarget = analyzedText.substring(analyzedText.length - 6)
        if (firstClassTarget == savedLotteryNumbers.firstClass) {
            _matchingRankResult.value = MatchingRank.FIRST
            return
        }

        val secondClassTarget = analyzedText.substring(analyzedText.length - 4)
        if (secondClassTarget == savedLotteryNumbers.secondClass) {
            _matchingRankResult.value = MatchingRank.SECOND
            return
        }

        val thirdClassTarget = analyzedText.substring(analyzedText.length - 2)
        if (thirdClassTarget == savedLotteryNumbers.thirdClassNumberPrimary ||
            thirdClassTarget == savedLotteryNumbers.thirdClassNumberSecondary ||
            thirdClassTarget == savedLotteryNumbers.thirdClassNumberTertiary) {
            _matchingRankResult.value = MatchingRank.THIRD
            return
        }
    }
}
