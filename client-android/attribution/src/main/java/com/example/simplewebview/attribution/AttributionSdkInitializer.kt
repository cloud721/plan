package com.example.simplewebview.attribution

import android.app.Application
import android.util.Log
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.appsflyer.deeplink.DeepLink
import com.appsflyer.deeplink.DeepLinkListener
import com.appsflyer.deeplink.DeepLinkResult
import com.example.simplewebview.attribution.providers.AppsFlyerInstallRelay
import com.example.simplewebview.attribution.providers.AppsFlyerLinkRelay

object AttributionSdkInitializer {
    private const val TAG = "AttributionSdkInit"

    fun initialize(application: Application, sdkConfig: AttributionSdkConfig) {
        initFacebook(application, sdkConfig)
        initAppsFlyer(application, sdkConfig)
    }

    private fun initFacebook(application: Application, sdkConfig: AttributionSdkConfig) {
        val appId = sdkConfig.facebookApplicationId()
        if (appId == null) {
            AttributionDebugStore.add("facebook", "disabled because app id is blank")
            return
        }
        val initializedAppId = FacebookAttributionSupport.initializeSdk(application, appId)
        AttributionDebugStore.add("facebook", "initialized Facebook SDK appId=$initializedAppId")
    }

    private fun initAppsFlyer(application: Application, sdkConfig: AttributionSdkConfig) {
        if (!sdkConfig.isAppsFlyerEnabled()) {
            AttributionDebugStore.add("appsflyer", "disabled because dev key is blank")
            return
        }

        AttributionDebugStore.add("appsflyer", "initializing AppsFlyer SDK")
        val appsFlyer = AppsFlyerLib.getInstance()
        appsFlyer.subscribeForDeepLink(object : DeepLinkListener {
            override fun onDeepLinking(result: DeepLinkResult) {
                when (result.status) {
                    DeepLinkResult.Status.FOUND -> {
                        val deepLink = result.deepLink ?: return
                        AttributionDebugStore.add("appsflyer", "udl found deepLink=$deepLink")
                        AppsFlyerLinkRelay.emit(
                            AppLinkData(
                                sdk = LinkSdkType.APPSFLYER,
                                linkUrl = deepLink.readLinkUrl(),
                                ref = deepLink.toString(),
                                extras = buildExtras(deepLink),
                            )
                        )
                    }

                    DeepLinkResult.Status.NOT_FOUND -> {
                        AttributionDebugStore.add("appsflyer", "udl not found")
                        Log.d(TAG, "AppsFlyer deep link not found")
                    }

                    else -> {
                        AttributionDebugStore.add("appsflyer", "udl error=${result.error}")
                        Log.w(TAG, "AppsFlyer deep link error: ${result.error}")
                    }
                }
            }
        })
        appsFlyer.init(sdkConfig.appsFlyerDevKey, conversionListener(), application)
        appsFlyer.start(application)
    }

    private fun conversionListener(): AppsFlyerConversionListener {
        return object : AppsFlyerConversionListener {
            override fun onConversionDataSuccess(conversionData: MutableMap<String, Any>?) {
                if (conversionData.isNullOrEmpty()) return
                val extras = conversionData.entries.associate { (key, value) ->
                    key to value.toString()
                }
                AttributionDebugStore.add("appsflyer", "conversion success payload=$extras")
                AppsFlyerInstallRelay.emit(
                    AppLinkData(
                        sdk = LinkSdkType.APPSFLYER,
                        linkUrl = conversionLink(extras),
                        ref = conversionData.toString(),
                        extras = extras,
                    )
                )
            }

            override fun onConversionDataFail(errorMessage: String?) {
                AttributionDebugStore.add("appsflyer", "conversion failed=$errorMessage")
                Log.w(TAG, "AppsFlyer conversion data failed: $errorMessage")
            }

            override fun onAppOpenAttribution(attributionData: MutableMap<String, String>?) {
                AttributionDebugStore.add("appsflyer", "legacy app open attribution=$attributionData")
                Log.d(TAG, "AppsFlyer onAppOpenAttribution ignored because UDL is enabled")
            }

            override fun onAttributionFailure(errorMessage: String?) {
                AttributionDebugStore.add("appsflyer", "legacy attribution failure=$errorMessage")
                Log.w(TAG, "AppsFlyer attribution failure: $errorMessage")
            }
        }
    }

    private fun buildExtras(deepLink: DeepLink): Map<String, String> {
        val extras = linkedMapOf<String, String>()
        deepLink.readStringValue("deep_link_value")?.let { extras["deep_link_value"] = it }
        deepLink.readStringValue("deep_link_sub1")?.let { extras["deep_link_sub1"] = it }
        deepLink.readStringValue("deep_link_sub2")?.let { extras["deep_link_sub2"] = it }
        deepLink.readStringValue("link")?.let { extras["link"] = it }
        extras["is_deferred"] = deepLink.isDeferred.toString()
        return extras
    }

    private fun DeepLink.readLinkUrl(): String? {
        return readStringValue("link")
            ?: readStringValue("af_dp")
            ?: deepLinkValue
    }

    private fun DeepLink.readStringValue(key: String): String? {
        return runCatching { getStringValue(key) }.getOrNull()?.takeIf { it.isNotBlank() }
    }

    private fun conversionLink(extras: Map<String, String>): String? {
        return extras["link"]
            ?: extras["af_dp"]
            ?: extras["deep_link_value"]
            ?: extras["deep_link_sub1"]
    }
}
