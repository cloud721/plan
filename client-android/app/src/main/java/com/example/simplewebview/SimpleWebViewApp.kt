package com.example.simplewebview

import android.app.Application

class SimpleWebViewApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppAttribution.initialize(this)
    }
}
