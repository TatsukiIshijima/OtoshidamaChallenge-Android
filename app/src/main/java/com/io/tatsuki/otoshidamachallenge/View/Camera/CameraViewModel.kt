package com.io.tatsuki.otoshidamachallenge.View.Camera

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class CameraViewModel(application: Application) : AndroidViewModel(application) {

    val resultText = MutableLiveData<String>()
}
