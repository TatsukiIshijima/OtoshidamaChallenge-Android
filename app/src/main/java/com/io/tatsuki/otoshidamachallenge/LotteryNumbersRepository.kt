package com.io.tatsuki.otoshidamachallenge

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LotteryNumbersRepository(
    private val lotteryNumbersDataStore: LotteryNumbersDataStore
) {

    suspend fun loadLotteryNumbers(): LotteryNumbers =
        withContext(Dispatchers.IO) {
            lotteryNumbersDataStore.loadData().toLotteryNumbers()
        }

    suspend fun saveLotteryNumbers(lotteryNumbers: LotteryNumbers) =
        withContext(Dispatchers.IO) {
            lotteryNumbersDataStore.saveData(lotteryNumbers.toJson())
        }
}