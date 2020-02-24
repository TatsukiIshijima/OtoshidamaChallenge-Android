package com.io.tatsuki.otoshidamachallenge

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity

class LotteryNumbersDataStore(context: Context) {

    private companion object {
        const val DATA_STORE_NAME = "LotteryNumbersDataStore"
        const val DATA_KEY = "LotteryNumbersKey"
    }

    private var sharedPreferences: SharedPreferences =
        context.getSharedPreferences(DATA_STORE_NAME, AppCompatActivity.MODE_PRIVATE)
    private val editor = sharedPreferences.edit()

    fun saveData(lotteryNumbersJson: String) {
        editor.apply {
            putString(DATA_KEY, lotteryNumbersJson)
        }
    }

    fun loadData(): String {
        return sharedPreferences.getString(DATA_KEY, "") ?: ""
    }

}