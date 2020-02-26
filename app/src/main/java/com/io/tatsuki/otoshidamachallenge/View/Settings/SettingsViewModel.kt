package com.io.tatsuki.otoshidamachallenge.View.Settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.io.tatsuki.otoshidamachallenge.Event
import com.io.tatsuki.otoshidamachallenge.Model.LotteryNumbers
import com.io.tatsuki.otoshidamachallenge.OtoshidamaChallengeApplication
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val _lotteryNumbersEvent: MutableLiveData<Event<LotteryNumbers>> = MutableLiveData()
    val lotteryNumbersEvent: LiveData<Event<LotteryNumbers>> = _lotteryNumbersEvent

    fun getLotteryNumbers() {
        val application = getApplication<OtoshidamaChallengeApplication>()
        viewModelScope.launch {
            _lotteryNumbersEvent.value =
                Event(
                    application.otoshidamaChallengeAppContainer.lotteryNumbersRepository.loadLotteryNumbers()
                )
        }
    }

    suspend fun saveLotteryNumbers(lotteryNumbers: LotteryNumbers) {
        val application = getApplication<OtoshidamaChallengeApplication>()
        viewModelScope.launch {
            application.
                otoshidamaChallengeAppContainer.
                lotteryNumbersRepository.
                saveLotteryNumbers(lotteryNumbers)
        }
    }
}
