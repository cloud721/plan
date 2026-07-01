package com.example.simplewebview.attribution

import android.content.Intent
import android.net.Uri

class StartupLinkIntake(
    private val cache: RawLinkCache,
) {
    fun intake(intent: Intent?): Uri? {
        if (intent == null) return null
        val action = intent.action ?: return null
        if (action != Intent.ACTION_VIEW && action != Intent.ACTION_MAIN) return null
        val uri = intent.data ?: return null
        cache.putRawStartupUri(uri.toString())
        AttributionDebugStore.add("intake", "cached startup uri=$uri")
        return uri
    }
}
