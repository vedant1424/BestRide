package com.example.bestride

import android.app.Application
import com.google.android.libraries.places.api.Places
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class BestrideApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Places.initialize(applicationContext, "AIzaSyDKqjOVYUgRi-7zCeUNnBYhOBcfsmF4TJc")
    }
}