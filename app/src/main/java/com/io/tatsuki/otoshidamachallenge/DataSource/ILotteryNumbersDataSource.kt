package com.io.tatsuki.otoshidamachallenge.DataSource

interface ILotteryNumbersDataSource {

    suspend fun saveData(lotteryNumbersJson: String)

    suspend fun loadData(): String
}