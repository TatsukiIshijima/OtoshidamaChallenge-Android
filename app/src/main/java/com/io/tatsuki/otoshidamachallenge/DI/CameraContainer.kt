package com.io.tatsuki.otoshidamachallenge.DI

import com.io.tatsuki.otoshidamachallenge.Repository.ILotteryNumbersRepository

class CameraContainer(repository: ILotteryNumbersRepository) {

    val cameraViewModelFactory = CameraViewModelFactory(repository)
}