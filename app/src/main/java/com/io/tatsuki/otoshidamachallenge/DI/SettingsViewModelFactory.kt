package com.io.tatsuki.otoshidamachallenge.DI

import com.io.tatsuki.otoshidamachallenge.Repository.ILotteryNumbersRepository
import com.io.tatsuki.otoshidamachallenge.View.Settings.SettingsViewModel

class SettingsViewModelFactory(
    private val lotteryNumbersRepository: ILotteryNumbersRepository
) : Factory<SettingsViewModel> {
    override fun create(): SettingsViewModel {
        return SettingsViewModel(lotteryNumbersRepository)
    }
}