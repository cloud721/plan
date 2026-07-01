package com.example.simplewebview.attribution.providers

import com.example.simplewebview.attribution.AppLinkData
import com.example.simplewebview.attribution.InstallReferrerProvider

class AppsFlyerInstallConversionProvider : InstallReferrerProvider {
    override fun requestInstallReferrer(onResult: (AppLinkData) -> Unit) {
        AppsFlyerInstallRelay.observe(onResult)
    }
}
