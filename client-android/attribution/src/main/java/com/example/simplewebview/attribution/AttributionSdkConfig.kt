package com.example.simplewebview.attribution

data class AttributionSdkConfig(
    val appsFlyerDevKey: String = "",
    val facebookAppId: String? = null,
    val clipboardEnabled: Boolean = true,
) {
    fun isAppsFlyerEnabled(): Boolean {
        return appsFlyerDevKey.isNotBlank()
    }

    fun facebookApplicationId(): String? {
        return FacebookAttributionSupport.applicationId(facebookAppId)
    }
}
