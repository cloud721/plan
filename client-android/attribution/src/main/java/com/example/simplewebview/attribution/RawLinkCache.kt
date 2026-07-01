package com.example.simplewebview.attribution

import android.content.Context

interface RawLinkCache {
    fun putRawStartupUri(value: String)
    fun getRawStartupUri(): String?
    fun clearRawStartupUri()
}

class SharedPrefsRawLinkCache(context: Context) : RawLinkCache {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun putRawStartupUri(value: String) {
        prefs.edit().putString(KEY_STARTUP_URI, value).apply()
    }

    override fun getRawStartupUri(): String? = prefs.getString(KEY_STARTUP_URI, null)

    override fun clearRawStartupUri() {
        prefs.edit().remove(KEY_STARTUP_URI).apply()
    }

    private companion object {
        const val PREFS_NAME = "attribution_cache"
        const val KEY_STARTUP_URI = "startup_uri"
    }
}
