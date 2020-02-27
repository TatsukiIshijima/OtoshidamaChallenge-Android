package com.io.tatsuki.otoshidamachallenge.View.Settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.io.tatsuki.otoshidamachallenge.Event
import com.io.tatsuki.otoshidamachallenge.Model.LotteryNumbers
import com.io.tatsuki.otoshidamachallenge.Repository.ILotteryNumbersRepository
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: ILotteryNumbersRepository
) : ViewModel() {

    private val _lotteryNumbersEvent: MutableLiveData<Event<LotteryNumbers>> = MutableLiveData()
    val lotteryNumbersEvent: LiveData<Event<LotteryNumbers>> = _lotteryNumbersEvent

    fun getLotteryNumbers() {
        viewModelScope.launch {
            _lotteryNumbersEvent.value = Event(repository.loadLotteryNumbers())
        }
    }

    suspend fun saveLotteryNumbers(lotteryNumbers: LotteryNumbers) {
        viewModelScope.launch {
            repository.loadLotteryNumbers()
        }
    }
}
