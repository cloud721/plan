package com.example.simplewebview.attribution.providers

import com.example.simplewebview.attribution.AppLinkData

object AppsFlyerInstallRelay {
    private val pendingResults = mutableListOf<AppLinkData>()
    private val observers = mutableListOf<(AppLinkData) -> Unit>()

    @Synchronized
    fun emit(data: AppLinkData) {
        pendingResults.add(data)
        val snapshot = observers.toList()
        snapshot.forEach { observer -> observer(data) }
    }

    @Synchronized
    fun observe(onResult: (AppLinkData) -> Unit) {
        observers.add(onResult)
        val snapshot = pendingResults.toList()
        pendingResults.clear()
        snapshot.forEach(onResult)
    }
}
