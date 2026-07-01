package com.example.simplewebview

import com.example.simplewebview.attribution.AttributionSdkConfig

object AppAttributionConfig {
    fun sdkConfig(): AttributionSdkConfig {
        return AttributionSdkConfig(
            appsFlyerDevKey = BuildConfig.APPSFLYER_DEV_KEY,
            facebookAppId = BuildConfig.FACEBOOK_APP_ID,
        )
    }
}
