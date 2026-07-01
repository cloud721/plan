package com.example.simplewebview.attribution

import android.net.Uri

data class AttributionRoutingSpec(
    val actionBuilder: (screen: String, contentId: String?) -> String,
    val isExecutableAction: (String) -> Boolean,
    val contentIdFromAction: (String) -> String?,
    val providerFieldExtractors: List<(Uri, AppLinkData) -> AnalyticsResult?> = emptyList(),
    val regexFallback: (String, LinkSdkType) -> AnalyticsResult? = { _, _ -> null },
)
