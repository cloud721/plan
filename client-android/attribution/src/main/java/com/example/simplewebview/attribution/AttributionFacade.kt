package com.example.simplewebview.attribution

class AttributionFacade(
    private val runtimeProviders: List<RuntimeLinkProvider>,
    private val installProviders: List<InstallReferrerProvider>,
) {
    fun handleRuntimeLinks(onResult: (AppLinkData) -> Unit) {
        runtimeProviders.forEach { provider ->
            provider.handleAppLink(onResult)
        }
    }

    fun handleInstallReferrers(onResult: (AppLinkData) -> Unit) {
        installProviders.forEach { provider ->
            provider.requestInstallReferrer(onResult)
        }
    }
}
