package com.io.tatsuki.otoshidamachallenge.Repository

import com.io.tatsuki.otoshidamachallenge.Model.LotteryNumbers

interface ILotteryNumbersRepository {

    suspend fun loadLotteryNumbers(): LotteryNumbers

    suspend fun saveLotteryNumbers(lotteryNumbers: LotteryNumbers)
}