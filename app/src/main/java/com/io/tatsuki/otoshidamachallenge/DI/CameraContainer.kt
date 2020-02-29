package com.io.tatsuki.otoshidamachallenge.DI

import com.io.tatsuki.otoshidamachallenge.Repository.LotteryNumbersRepository

class CameraContainer(repository: LotteryNumbersRepository) {

    val cameraViewModelFactory = CameraViewModelFactory(repository)
}