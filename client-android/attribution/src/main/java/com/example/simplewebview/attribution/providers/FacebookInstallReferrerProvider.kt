package com.example.simplewebview.attribution.providers

import android.content.Context
import android.util.Log
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.example.simplewebview.attribution.AppLinkData
import com.example.simplewebview.attribution.AttributionDebugStore
import com.example.simplewebview.attribution.FacebookAttributionSupport
import com.example.simplewebview.attribution.InstallReferrerProvider
import com.example.simplewebview.attribution.LinkSdkType

class FacebookInstallReferrerProvider(
    context: Context,
) : InstallReferrerProvider {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun requestInstallReferrer(onResult: (AppLinkData) -> Unit) {
        if (prefs.getBoolean(KEY_CONSUMED, false)) {
            AttributionDebugStore.add("facebook-referrer", "skip because already consumed")
            return
        }

        val client = InstallReferrerClient.newBuilder(appContext).build()
        client.startConnection(object : InstallReferrerStateListener {
            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                when (responseCode) {
                    InstallReferrerClient.InstallReferrerResponse.OK -> {
                        runCatching {
                            val details = client.installReferrer
                            val rawReferrer = details.installReferrer
                            if (!rawReferrer.isNullOrBlank() && FacebookAttributionSupport.isFacebookInstallReferrer(rawReferrer)) {
                                AttributionDebugStore.add("facebook-referrer", "received rawReferrer=$rawReferrer")
                                onResult(
                                    AppLinkData(
                                        sdk = LinkSdkType.FACEBOOK,
                                        linkUrl = rawReferrer,
                                        ref = rawReferrer,
                                        clickTimestamp = details.referrerClickTimestampSeconds,
                                        extras = mapOf(
                                            "install_begin_ts" to details.installBeginTimestampSeconds.toString(),
                                            "google_play_instant" to details.googlePlayInstantParam.toString(),
                                        ),
                                    )
                                )
                            } else {
                                AttributionDebugStore.add("facebook-referrer", "ignore non-facebook referrer=$rawReferrer")
                            }
                            markConsumed()
                        }.onFailure { error ->
                            Log.w(TAG, "Failed to read Facebook install referrer", error)
                        }
                        client.endConnection()
                    }

                    InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED,
                    InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE,
                    InstallReferrerClient.InstallReferrerResponse.DEVELOPER_ERROR,
                    InstallReferrerClient.InstallReferrerResponse.PERMISSION_ERROR -> {
                        AttributionDebugStore.add("facebook-referrer", "unavailable responseCode=$responseCode")
                        markConsumed()
                        client.endConnection()
                    }
                }
            }

            override fun onInstallReferrerServiceDisconnected() {
                AttributionDebugStore.add("facebook-referrer", "service disconnected")
                client.endConnection()
            }
        })
    }

    private fun markConsumed() {
        prefs.edit().putBoolean(KEY_CONSUMED, true).apply()
    }

    private companion object {
        const val PREFS_NAME = "facebook_install_referrer"
        const val KEY_CONSUMED = "consumed"
        const val TAG = "FacebookReferrer"
    }
}
