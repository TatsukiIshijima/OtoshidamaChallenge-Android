package com.io.tatsuki.otoshidamachallenge.DI

import com.io.tatsuki.otoshidamachallenge.Repository.LotteryNumbersRepository
import com.io.tatsuki.otoshidamachallenge.View.Camera.CameraViewModel

class CameraViewModelFactory(
    private val lotteryNumbersRepository: LotteryNumbersRepository
) : Factory<CameraViewModel> {
    override fun create(): CameraViewModel {
        return CameraViewModel(lotteryNumbersRepository)
    }
}