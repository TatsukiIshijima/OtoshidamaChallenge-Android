package com.io.tatsuki.otoshidamachallenge

import android.app.Application
import com.io.tatsuki.otoshidamachallenge.DI.AppContainer

class OtoshidamaChallengeApplication : Application() {

    lateinit var appContainer: AppContainer

    override fun onCreate() {
        super.onCreate()

        appContainer = AppContainer(this.applicationContext)
    }
}