package com.io.tatsuki.otoshidamachallenge.View.Settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.io.tatsuki.otoshidamachallenge.Model.LotteryNumbers
import com.io.tatsuki.otoshidamachallenge.Repository.ILotteryNumbersRepository
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: ILotteryNumbersRepository
) : ViewModel() {

    private val _lotteryNumbersEvent: MutableLiveData<LotteryNumbers> = MutableLiveData()
    val lotteryNumbersEvent: LiveData<LotteryNumbers> = _lotteryNumbersEvent

    fun getLotteryNumbers() {
        viewModelScope.launch {
            _lotteryNumbersEvent.value = repository.loadLotteryNumbers()
        }
    }

    fun saveLotteryNumbers(lotteryNumbers: LotteryNumbers) {
        viewModelScope.launch {
            repository.saveLotteryNumbers(lotteryNumbers)
        }
    }

    fun validateLength(numberText: String, length: Int): Boolean {
        return numberText.length == length
    }

    fun removeWildCard(numberText: String): String {
        return numberText.replace("*", "")
    }
}
