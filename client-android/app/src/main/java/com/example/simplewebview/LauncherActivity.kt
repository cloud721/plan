package com.example.simplewebview

import android.app.Activity
import android.content.Intent
import android.os.Bundle

class LauncherActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppAttribution.startupIntake(this).intake(intent)
        openLogin()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        AppAttribution.startupIntake(this).intake(intent)
        openLogin()
    }

    private fun openLogin() {
        startActivity(AppAttribution.buildLoginIntent(this))
        finish()
    }
}
