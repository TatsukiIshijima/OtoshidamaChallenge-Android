package com.io.tatsuki.otoshidamachallenge.DataSource

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity

class LotteryNumbersDataSource(context: Context) : ILotteryNumbersDataSource {

    private companion object {
        const val FILE_NAME = "LotteryNumbersDataSource"
        const val DATA_KEY = "LotteryNumbersKey"
    }

    private var sharedPreferences: SharedPreferences =
        context.getSharedPreferences(FILE_NAME, AppCompatActivity.MODE_PRIVATE)
    private val editor = sharedPreferences.edit()

    override suspend fun saveData(lotteryNumbersJson: String) {
        editor.apply {
            putString(DATA_KEY, lotteryNumbersJson)
        }.commit()

    }

    override suspend fun loadData(): String {
        return sharedPreferences.getString(DATA_KEY, "") ?: ""
    }

}