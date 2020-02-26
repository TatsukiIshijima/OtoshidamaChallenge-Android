package com.io.tatsuki.otoshidamachallenge

import android.app.Application
import com.io.tatsuki.otoshidamachallenge.DI.OtoshidamaChallengeAppContainer

class OtoshidamaChallengeApplication : Application() {

    lateinit var otoshidamaChallengeAppContainer: OtoshidamaChallengeAppContainer

    override fun onCreate() {
        super.onCreate()

        otoshidamaChallengeAppContainer = OtoshidamaChallengeAppContainer(applicationContext)
    }
}