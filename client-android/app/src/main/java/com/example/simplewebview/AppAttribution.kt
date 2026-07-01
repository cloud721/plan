package com.example.simplewebview

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.simplewebview.attribution.AnalyticsResult
import com.example.simplewebview.attribution.AppLinkData
import com.example.simplewebview.attribution.AttributionKit
import com.example.simplewebview.attribution.AttributionRoutingSpec
import com.example.simplewebview.attribution.DebugStoreLinkReporter
import com.example.simplewebview.attribution.HomeAttributionCoordinator
import com.example.simplewebview.attribution.LinkDispatcher
import com.example.simplewebview.attribution.LinkSdkType
import com.example.simplewebview.attribution.StartupLinkIntake

object AppAttribution {
    private const val ACTION_SCHEME = "simplewebview"
    private const val ACTION_HOST = "route"
    private const val EXTRA_TARGET_TAB = "target_tab"
    private const val TAG = "AppAttribution"

    fun initialize(application: Application) {
        AttributionKit.initialize(application, AppAttributionConfig.sdkConfig())
    }

    fun startupIntake(context: Context): StartupLinkIntake {
        return AttributionKit.startupIntake(context)
    }

    fun createCoordinator(context: Context): HomeAttributionCoordinator {
        return AttributionKit.createCoordinator(
            context = context,
            sdkConfig = AppAttributionConfig.sdkConfig(),
            routing = routingSpec(),
            dispatcher = AppLinkDispatcher(context),
            reporter = DebugStoreLinkReporter(TAG),
            autoAttachContent = { contentId ->
                Log.d(TAG, "auto attach contentId=$contentId")
            },
        )
    }

    fun buildLoginIntent(context: Context): Intent {
        return Intent(context, LoginActivity::class.java)
    }

    fun resolveSdk(rawValue: String): LinkSdkType {
        return AttributionKit.resolveSdk(rawValue)
    }

    fun targetTabExtra(): String = EXTRA_TARGET_TAB

    private fun routingSpec(): AttributionRoutingSpec {
        return AttributionRoutingSpec(
            actionBuilder = ::buildAction,
            isExecutableAction = ::isExecutableAction,
            contentIdFromAction = ::contentIdFromAction,
            providerFieldExtractors = listOf(::extractProviderFields),
            regexFallback = ::regexFallback,
        )
    }

    private fun buildAction(screen: String, contentId: String?): String {
        return Uri.Builder()
            .scheme(ACTION_SCHEME)
            .authority(ACTION_HOST)
            .appendQueryParameter("screen", screen)
            .apply {
                if (!contentId.isNullOrBlank()) {
                    appendQueryParameter("contentId", contentId)
                }
            }
            .build()
            .toString()
    }

    private fun isExecutableAction(value: String): Boolean {
        val uri = runCatching { Uri.parse(value) }.getOrNull() ?: return false
        return uri.scheme == ACTION_SCHEME && uri.host == ACTION_HOST
    }

    private fun contentIdFromAction(value: String): String? {
        return runCatching { Uri.parse(value).getQueryParameter("contentId") }.getOrNull()
    }

    private fun extractProviderFields(uri: Uri, data: AppLinkData): AnalyticsResult? {
        val deepLinkValue = uri.getQueryParameter("deep_link_value")
            ?: uri.getQueryParameter("af_dp")
            ?: uri.getQueryParameter("target_url")
            ?: uri.getQueryParameter("applink_url")
            ?: data.extras["link"]
            ?: data.extras["af_dp"]
            ?: data.extras["deep_link_value"]
            ?: data.extras["deep_link_sub1"]
            ?: data.extras["target_url"]
            ?: data.extras["applink_url"]
            ?: return null
        val parsed = runCatching { Uri.parse(deepLinkValue) }.getOrNull()
        val screen = parsed?.getQueryParameter("screen")
            ?: parsed?.lastPathSegment
            ?: deepLinkValue.takeIf {
                it.lowercase() in setOf("ecommerce", "booking", "activity", "yearly_plan", "debug_attribution")
            }
            ?: return null
        val contentId = parsed?.getQueryParameter("contentId") ?: data.extras["contentId"] ?: data.extras["bookid"]
        return AnalyticsResult(
            contentId = contentId,
            action = buildAction(screen.lowercase(), contentId),
        )
    }

    private fun regexFallback(raw: String, sdk: LinkSdkType): AnalyticsResult? {
        val normalized = raw.lowercase()
        val screen = when {
            "yearly" in normalized || "plan" in normalized || "player" in normalized -> "yearly_plan"
            "booking" in normalized || "hotel" in normalized || "map" in normalized -> "booking"
            "activity" in normalized || "feed" in normalized || "notification" in normalized -> "activity"
            "ecommerce" in normalized || "shop" in normalized || "product" in normalized -> "ecommerce"
            sdk == LinkSdkType.UNIVERSAL -> "activity"
            else -> return null
        }
        return AnalyticsResult(
            contentId = null,
            action = buildAction(screen, null),
        )
    }

    private class AppLinkDispatcher(private val context: Context) : LinkDispatcher {
        override fun dispatch(action: String): Boolean {
            val uri = runCatching { Uri.parse(action) }.getOrNull() ?: return false
            val screen = uri.getQueryParameter("screen") ?: return false
            val contentId = uri.getQueryParameter("contentId")
            if (context is MainActivity) {
                context.openAttributionTarget(screen, contentId)
                return true
            }
            val intent = when (screen) {
                "debug_attribution" -> Intent(context, AttributionDebugActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                "yearly_plan" -> Intent(context, YearlyPlanActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    putExtra("content_id", contentId)
                }

                "ecommerce", "booking", "activity" -> Intent(context, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    putExtra(EXTRA_TARGET_TAB, screen)
                    putExtra("content_id", contentId)
                }

                else -> return false
            }
            context.startActivity(intent)
            return true
        }
    }
}
