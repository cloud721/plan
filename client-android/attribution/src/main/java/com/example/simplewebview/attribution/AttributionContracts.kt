package com.example.simplewebview.attribution

data class AppLinkData(
    val sdk: LinkSdkType,
    val linkUrl: String? = null,
    val ref: String? = null,
    val clickTimestamp: Long? = null,
    val minAppVersion: Int? = null,
    val extras: Map<String, String> = emptyMap(),
)

data class AnalyticsResult(
    val contentId: String? = null,
    val action: String? = null,
    val fromOpenWith: Boolean = false,
    val needOpen: Boolean = true,
)

data class LinkExecutionResult(
    val opened: Boolean,
    val uploadType: Int,
    val contentId: String? = null,
    val action: String? = null,
)

enum class LinkSdkType {
    NONE,
    FACEBOOK,
    FIREBASE,
    APPSFLYER,
    HUAWEI,
    KOCHAVA,
    ADJUST,
    GOOGLE,
    CLIPBOARD,
    UNIVERSAL,
    GOOGLE_ENGAGE,
    SERVER_DEFAULT,
    UAC,
    KOC,
}

enum class LaunchType {
    COLD_START,
    BACKGROUND_RETURN,
}

interface RuntimeLinkProvider {
    fun handleAppLink(onResult: (AppLinkData) -> Unit)
}

interface InstallReferrerProvider {
    fun requestInstallReferrer(onResult: (AppLinkData) -> Unit)
}

interface LinkParser {
    fun parse(data: AppLinkData): AnalyticsResult?
    fun parse(raw: String, sdk: LinkSdkType): AnalyticsResult?
}

interface LinkDispatcher {
    fun dispatch(action: String): Boolean
}

interface LinkReporter {
    fun upload(
        rawValue: String,
        result: AnalyticsResult?,
        launchType: LaunchType,
        uploadType: Int,
        opened: Boolean,
        durationMs: Long,
    )
}
