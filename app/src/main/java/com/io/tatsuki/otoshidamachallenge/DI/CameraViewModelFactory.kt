package com.io.tatsuki.otoshidamachallenge.DI

import com.io.tatsuki.otoshidamachallenge.Repository.ILotteryNumbersRepository
import com.io.tatsuki.otoshidamachallenge.View.Camera.CameraViewModel

class CameraViewModelFactory(
    private val lotteryNumbersRepository: ILotteryNumbersRepository
) : Factory<CameraViewModel> {
    override fun create(): CameraViewModel {
        return CameraViewModel(lotteryNumbersRepository)
    }
}