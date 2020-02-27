package com.io.tatsuki.otoshidamachallenge.DI

import android.content.Context
import com.io.tatsuki.otoshidamachallenge.DataSource.LotteryNumbersDataSource
import com.io.tatsuki.otoshidamachallenge.Repository.LotteryNumbersRepository

// 手動での依存注入参考：https://developer.android.com/training/dependency-injection/manual
class AppContainer constructor(context: Context) {

    private val lotteryNumbersDataSource = LotteryNumbersDataSource(context)

    val lotteryNumbersRepository = LotteryNumbersRepository(lotteryNumbersDataSource)

    var settingsContainer: SettingsContainer? = null
}