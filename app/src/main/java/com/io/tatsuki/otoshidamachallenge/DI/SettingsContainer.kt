package com.io.tatsuki.otoshidamachallenge.DI

import com.io.tatsuki.otoshidamachallenge.Repository.ILotteryNumbersRepository

class SettingsContainer(repository: ILotteryNumbersRepository) {

    val settingsViewModelFactory = SettingsViewModelFactory(repository)
}