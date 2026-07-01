package com.example.simplewebview.attribution

import android.content.Intent

class HomeAttributionCoordinator(
    private val rawLinkCache: RawLinkCache,
    private val facade: AttributionFacade,
    private val executor: AttributionExecutor,
) {
    fun onCreate(launchType: LaunchType) {
        AttributionDebugStore.add("coordinator", "onCreate launchType=$launchType")
        consumeCachedStartupLink(launchType)
        facade.handleRuntimeLinks { data ->
            executor.consumeProviderResult(
                data = data,
                launchType = launchType,
            )
        }
    }

    fun onNewIntent(intent: Intent?, launchType: LaunchType, sdkResolver: (String) -> LinkSdkType) {
        val rawValue = intent?.dataString
        if (!rawValue.isNullOrBlank()) {
            AttributionDebugStore.add("coordinator", "onNewIntent raw=$rawValue launchType=$launchType")
            executor.consumeCachedIntent(
                rawValue = rawValue,
                sdk = sdkResolver(rawValue),
                launchType = launchType,
            )
        }
    }

    fun onFirstInstallEligible(launchType: LaunchType) {
        AttributionDebugStore.add("coordinator", "request install referrers launchType=$launchType")
        facade.handleInstallReferrers { data ->
            executor.consumeProviderResult(
                data = data,
                launchType = launchType,
                fromInstallRef = true,
            )
        }
    }

    fun resetForNextLaunchCycle() {
        executor.resetLaunchCycle()
    }

    private fun consumeCachedStartupLink(launchType: LaunchType) {
        val rawValue = rawLinkCache.getRawStartupUri() ?: return
        rawLinkCache.clearRawStartupUri()
        AttributionDebugStore.add("coordinator", "consume cached startup raw=$rawValue")
        executor.consumeCachedIntent(
            rawValue = rawValue,
            sdk = LinkSdkType.NONE,
            launchType = launchType,
        )
    }
}
