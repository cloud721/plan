package com.example.simplewebview.attribution

import android.net.Uri
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class AttributionParser(
    private val actionBuilder: (screen: String, contentId: String?) -> String,
    private val isExecutableAction: (String) -> Boolean,
    private val contentIdFromAction: (String) -> String?,
    private val providerFieldExtractors: List<(Uri, AppLinkData) -> AnalyticsResult?> = emptyList(),
    private val regexFallback: (String, LinkSdkType) -> AnalyticsResult? = { _, _ -> null },
) : LinkParser {

    override fun parse(data: AppLinkData): AnalyticsResult? {
        data.linkUrl?.let { parse(it, data.sdk) }?.let { return it }
        data.ref?.let { parse(it, data.sdk) }?.let { return it }
        return null
    }

    override fun parse(raw: String, sdk: LinkSdkType): AnalyticsResult? {
        if (raw.isBlank()) return null
        if (isExecutableAction(raw)) {
            return AnalyticsResult(
                contentId = contentIdFromAction(raw),
                action = raw,
            )
        }

        parseRawOpenWith(raw)?.let { return it }

        val uri = runCatching { Uri.parse(raw) }.getOrNull()
        if (uri != null) {
            parseOpenWith(uri)?.let { return it }

            uri.getQueryParameter("ndactionstr")
                ?.takeIf { isExecutableAction(it) }
                ?.let { action ->
                    return AnalyticsResult(
                        contentId = contentIdFromAction(action),
                        action = action,
                    )
                }

            parseGenericRoute(uri)?.let { return it }

            val seed = AppLinkData(sdk = sdk, linkUrl = raw)
            providerFieldExtractors.forEach { extractor ->
                extractor(uri, seed)?.let { return it }
            }
        }

        parseReferrerQuery(raw, sdk)?.let { return it }

        return regexFallback(raw, sdk)
    }

    private fun parseRawOpenWith(raw: String): AnalyticsResult? {
        val normalized = raw.lowercase()
        val looksLikeOpenWith = normalized.contains("://openwith?")
            || normalized.contains("/openwith?")
            || normalized.startsWith("openwith?")
        if (!looksLikeOpenWith) return null
        val query = raw.substringAfter('?', missingDelimiterValue = "")
        if (query.isBlank()) return null
        val params = parseQueryParams(query)
        val contentId = params["bookid"] ?: return null
        val needOpen = params["needOpen"] != "0"
        return AnalyticsResult(
            contentId = contentId,
            action = actionBuilder("yearly_plan", contentId),
            fromOpenWith = true,
            needOpen = needOpen,
        )
    }

    private fun parseOpenWith(uri: Uri): AnalyticsResult? {
        val path = uri.path
        val host = uri.host
        if (path != "/openwith" && path != "openwith" && host != "openwith") return null
        val params = parseQueryParams(uri.encodedQuery.orEmpty())
        val contentId = params["bookid"] ?: return null
        val needOpen = params["needOpen"] != "0"
        return AnalyticsResult(
            contentId = contentId,
            action = actionBuilder("yearly_plan", contentId),
            fromOpenWith = true,
            needOpen = needOpen,
        )
    }

    private fun parseGenericRoute(uri: Uri): AnalyticsResult? {
        val params = parseQueryParams(uri.encodedQuery.orEmpty())
        val screen = params["screen"]
            ?: params["page"]
            ?: params["target"]
            ?: uri.host
            ?: uri.lastPathSegment
            ?: return null
        val normalized = screen.lowercase()
        if (normalized !in SUPPORTED_SCREENS) return null
        val contentId = params["contentId"] ?: params["bookid"]
        return AnalyticsResult(
            contentId = contentId,
            action = actionBuilder(normalized, contentId),
        )
    }

    private fun parseReferrerQuery(raw: String, sdk: LinkSdkType): AnalyticsResult? {
        if (!raw.contains("=")) return null
        val params = parseQueryParams(raw)

        params["deep_link"]
            ?.let { parse(it, sdk) }
            ?.let { return it }

        params["link"]
            ?.let { parse(it, sdk) }
            ?.let { return it }

        params["af_dp"]
            ?.let { parse(it, sdk) }
            ?.let { return it }

        params["target_url"]
            ?.let { parse(it, sdk) }
            ?.let { return it }

        params["applink_url"]
            ?.let { parse(it, sdk) }
            ?.let { return it }

        params["deeplink"]
            ?.let { parse(it, sdk) }
            ?.let { return it }

        val screen = params["screen"]
            ?: params["page"]
            ?: params["target"]
        val contentId = params["contentId"] ?: params["bookid"]
        if (screen != null && screen.lowercase() in SUPPORTED_SCREENS) {
            return AnalyticsResult(
                contentId = contentId,
                action = actionBuilder(screen.lowercase(), contentId),
            )
        }
        return null
    }

    private fun parseQueryParams(raw: String): Map<String, String> {
        return raw.split("&")
            .mapNotNull { part ->
                if (part.isBlank()) return@mapNotNull null
                val separatorIndex = part.indexOf('=')
                val key = if (separatorIndex >= 0) part.substring(0, separatorIndex) else part
                val value = if (separatorIndex >= 0) part.substring(separatorIndex + 1) else ""
                decode(key) to decode(value)
            }
            .toMap()
    }

    private fun decode(value: String): String {
        return runCatching {
            URLDecoder.decode(value, StandardCharsets.UTF_8.name())
        }.getOrDefault(value)
    }

    private companion object {
        val SUPPORTED_SCREENS = setOf("ecommerce", "booking", "activity", "yearly_plan", "debug_attribution")
    }
}
