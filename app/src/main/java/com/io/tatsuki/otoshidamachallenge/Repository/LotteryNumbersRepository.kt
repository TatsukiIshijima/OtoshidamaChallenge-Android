package com.io.tatsuki.otoshidamachallenge.Repository

import com.io.tatsuki.otoshidamachallenge.DataSource.ILotteryNumbersDataSource
import com.io.tatsuki.otoshidamachallenge.Model.LotteryNumbers
import com.io.tatsuki.otoshidamachallenge.Model.toJson
import com.io.tatsuki.otoshidamachallenge.Model.toLotteryNumbers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LotteryNumbersRepository(
    private val lotteryNumbersDataStore: ILotteryNumbersDataSource
) : ILotteryNumbersRepository {

    var cacheLotteryNumbers: LotteryNumbers? = null

    override suspend fun loadLotteryNumbers(): LotteryNumbers =
        withContext(Dispatchers.IO) {
            val lotteryNumbers = lotteryNumbersDataStore.loadData()
            cacheLotteryNumbers = lotteryNumbers.toLotteryNumbers()
            lotteryNumbers.toLotteryNumbers()
        }

    override suspend fun saveLotteryNumbers(lotteryNumbers: LotteryNumbers) =
        withContext(Dispatchers.IO) {
            lotteryNumbersDataStore.saveData(lotteryNumbers.toJson())
        }
}