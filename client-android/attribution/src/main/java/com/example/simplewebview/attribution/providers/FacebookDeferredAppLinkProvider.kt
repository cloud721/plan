package com.example.simplewebview.attribution.providers

import android.content.Context
import com.facebook.applinks.AppLinkData as FacebookAppLinkData
import com.example.simplewebview.attribution.AppLinkData
import com.example.simplewebview.attribution.AttributionDebugStore
import com.example.simplewebview.attribution.FacebookAttributionSupport
import com.example.simplewebview.attribution.LinkSdkType
import com.example.simplewebview.attribution.RuntimeLinkProvider

class FacebookDeferredAppLinkProvider(
    context: Context,
    private val facebookApplicationId: String,
) : RuntimeLinkProvider {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun handleAppLink(onResult: (AppLinkData) -> Unit) {
        if (prefs.getBoolean(KEY_CONSUMED, false)) {
            AttributionDebugStore.add("facebook-deferred", "skip because already consumed")
            return
        }

        runCatching { FacebookAttributionSupport.initializeSdk(appContext, facebookApplicationId) }
            .onFailure { error ->
                AttributionDebugStore.add("facebook-deferred", "sdk init failed=${error.message}")
                markConsumed()
            }
            .onSuccess {
                FacebookAppLinkData.fetchDeferredAppLinkData(appContext, facebookApplicationId) { deferredData ->
                    try {
                        if (deferredData == null) {
                            AttributionDebugStore.add("facebook-deferred", "no deferred app link")
                            return@fetchDeferredAppLinkData
                        }
                        val extras = linkedMapOf<String, String>()
                        extras.putAll(FacebookAttributionSupport.flattenBundle(deferredData.argumentBundle))
                        extras.putAll(FacebookAttributionSupport.flattenBundle(deferredData.refererData, "referer."))
                        deferredData.promotionCode?.let { extras["promotion_code"] = it }
                        deferredData.ref?.let { extras["fb_ref"] = it }
                        val linkUrl = deferredData.targetUri?.toString()
                            ?: extras["target_url"]
                            ?: extras["com.facebook.platform.APPLINK_NATIVE_URL"]
                        AttributionDebugStore.add(
                            "facebook-deferred",
                            "received target=$linkUrl ref=${deferredData.ref} extras=$extras",
                        )
                        onResult(
                            AppLinkData(
                                sdk = LinkSdkType.FACEBOOK,
                                linkUrl = linkUrl,
                                ref = deferredData.ref ?: linkUrl,
                                clickTimestamp = extras["com.facebook.platform.APPLINK_TAP_TIME_UTC"]?.toLongOrNull(),
                                extras = extras,
                            )
                        )
                    } finally {
                        markConsumed()
                    }
                }
            }
    }

    private fun markConsumed() {
        prefs.edit().putBoolean(KEY_CONSUMED, true).apply()
    }

    private companion object {
        const val PREFS_NAME = "facebook_deferred_app_link"
        const val KEY_CONSUMED = "consumed"
    }
}
