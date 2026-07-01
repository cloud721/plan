package com.example.simplewebview.attribution.providers

import com.example.simplewebview.attribution.RuntimeLinkProvider

class AppsFlyerRuntimeProvider : RuntimeLinkProvider {
    override fun handleAppLink(onResult: (com.example.simplewebview.attribution.AppLinkData) -> Unit) {
        AppsFlyerLinkRelay.observe(onResult)
    }
}
