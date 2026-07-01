package com.example.simplewebview.attribution

import android.content.Context
import android.os.Bundle
import com.facebook.FacebookSdk

object FacebookAttributionSupport {
    fun normalizeApplicationId(rawValue: String): String {
        return rawValue.trim().removePrefix("fb")
    }

    fun applicationId(rawValue: String?): String? {
        val normalized = normalizeApplicationId(rawValue.orEmpty())
        return normalized.takeIf { it.isNotBlank() }
    }

    fun isFacebookInstallReferrer(rawReferrer: String?): Boolean {
        val normalized = rawReferrer?.lowercase().orEmpty()
        if (normalized.isBlank()) return false
        return normalized.contains("facebook") || normalized.contains("fb")
    }

    @Suppress("DEPRECATION")
    fun initializeSdk(context: Context, rawAppId: String? = null): String? {
        val applicationId = applicationId(rawAppId) ?: return null
        FacebookSdk.setApplicationId(applicationId)
        FacebookSdk.sdkInitialize(context.applicationContext)
        FacebookSdk.fullyInitialize()
        return applicationId
    }

    fun flattenBundle(bundle: Bundle?, prefix: String = ""): Map<String, String> {
        if (bundle == null) return emptyMap()
        val values = linkedMapOf<String, String>()
        for (key in bundle.keySet()) {
            val nextKey = if (prefix.isBlank()) key else "$prefix$key"
            when (val value = bundle.get(key)) {
                is Bundle -> values.putAll(flattenBundle(value, "$nextKey."))
                null -> Unit
                else -> values[nextKey] = value.toString()
            }
        }
        return values
    }
}
