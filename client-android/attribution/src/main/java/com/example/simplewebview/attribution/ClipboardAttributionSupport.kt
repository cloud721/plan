package com.example.simplewebview.attribution

object ClipboardAttributionSupport {
    fun normalize(rawValue: String?): String? {
        return rawValue?.trim()?.takeIf { it.isNotBlank() }
    }

    fun isLikelyAttributionPayload(rawValue: String?): Boolean {
        val normalized = normalize(rawValue)?.lowercase() ?: return false
        return normalized.startsWith("simplewebview://")
            || normalized.startsWith("simpleapp://")
            || normalized.startsWith("http://")
            || normalized.startsWith("https://")
            || "bookid=" in normalized
            || "screen=" in normalized
            || "target_url=" in normalized
            || "applink_url=" in normalized
            || "deep_link" in normalized
            || "af_dp=" in normalized
            || "fbclid=" in normalized
            || "utm_" in normalized
    }

    fun shouldConsume(rawValue: String?, lastConsumed: String?): Boolean {
        val normalized = normalize(rawValue) ?: return false
        val normalizedLastConsumed = normalize(lastConsumed)
        if (!isLikelyAttributionPayload(normalized)) return false
        return normalized != normalizedLastConsumed
    }
}
