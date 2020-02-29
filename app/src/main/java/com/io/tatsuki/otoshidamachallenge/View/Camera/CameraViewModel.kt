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

    init {
        val analyzeObserver = Observer<String> {
            val classNumberText = classNumberAnalyzeResult.value ?: ""
            val lotteryNumberText = lotteryNumberAnalyzeResult.value ?: ""
            if (classNumberText.isNotEmpty() && lotteryNumberText.isNotEmpty()) {
                _combinedAnalyzeResult.value = "${classNumberText}-${lotteryNumberText}"
            }
        }
        _combinedAnalyzeResult.addSource(classNumberAnalyzeResult, analyzeObserver)
        _combinedAnalyzeResult.addSource(lotteryNumberAnalyzeResult, analyzeObserver)
    }

    fun getLotteryNumbers() {
        viewModelScope.launch {
            repository.loadLotteryNumbers()
        }
    }

    fun matchLotteryNumbers(analyzedText: String): MatchingRank {
        val savedLotteryNumbers =
            repository.cacheLotteryNumbers ?: return MatchingRank.NONE
        if (analyzedText.length <= 9) {
            return MatchingRank.NONE
        }
        val specialClassPrimaryTarget = analyzedText.substring(analyzedText.length - 10)
        if ((specialClassPrimaryTarget == "${savedLotteryNumbers.specialPrimaryForward}${savedLotteryNumbers.specialPrimaryBackward}") ||
            (specialClassPrimaryTarget == "${savedLotteryNumbers.specialSecondaryForward}${savedLotteryNumbers.specialSecondaryBackward}") ||
            (specialClassPrimaryTarget == "${savedLotteryNumbers.specialTertiaryForward}${savedLotteryNumbers.specialTertiaryBackward}") ||
            (specialClassPrimaryTarget == "${savedLotteryNumbers.specialQuaternaryForward}${savedLotteryNumbers.specialQuaternaryBackward}")) {
            return MatchingRank.SPECIAL_PRIMARY
        }

        val specialClassSecondaryTarget = analyzedText.substring(analyzedText.length - 7)
        if (specialClassSecondaryTarget == "${savedLotteryNumbers.specialQuinaryForward}${savedLotteryNumbers.specialQuinaryBackward}") {
            return MatchingRank.SPECIAL_SECONDARY
        }

        val firstClassTarget = analyzedText.substring(analyzedText.length - 6)
        if (firstClassTarget == savedLotteryNumbers.firstClass) {
            return MatchingRank.FIRST
        }

        val secondClassTarget = analyzedText.substring(analyzedText.length - 4)
        if (secondClassTarget == savedLotteryNumbers.secondClass) {
            return MatchingRank.SECOND
        }

        val thirdClassTarget = analyzedText.substring(analyzedText.length - 2)
        if (thirdClassTarget == savedLotteryNumbers.thirdClassNumberPrimary ||
            thirdClassTarget == savedLotteryNumbers.thirdClassNumberSecondary ||
            thirdClassTarget == savedLotteryNumbers.thirdClassNumberTertiary) {
            return MatchingRank.THIRD
        }
        return MatchingRank.NONE
    }
}
