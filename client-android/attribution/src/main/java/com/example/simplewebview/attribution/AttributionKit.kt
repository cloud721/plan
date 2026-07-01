package com.example.simplewebview.attribution

import android.app.Application
import android.content.Context
import android.net.Uri
import com.example.simplewebview.attribution.providers.AppsFlyerInstallConversionProvider
import com.example.simplewebview.attribution.providers.AppsFlyerRuntimeProvider
import com.example.simplewebview.attribution.providers.ClipboardAttributionProvider
import com.example.simplewebview.attribution.providers.FacebookDeferredAppLinkProvider
import com.example.simplewebview.attribution.providers.FacebookInstallReferrerProvider
import com.example.simplewebview.attribution.providers.GoogleInstallReferrerProvider

object AttributionKit {
    fun initialize(application: Application, sdkConfig: AttributionSdkConfig) {
        AttributionSdkInitializer.initialize(application, sdkConfig)
    }

    fun startupIntake(context: Context): StartupLinkIntake {
        return StartupLinkIntake(rawLinkCache(context))
    }

    fun rawLinkCache(context: Context): RawLinkCache {
        return SharedPrefsRawLinkCache(context.applicationContext)
    }

    fun createCoordinator(
        context: Context,
        sdkConfig: AttributionSdkConfig,
        routing: AttributionRoutingSpec,
        dispatcher: LinkDispatcher,
        reporter: LinkReporter = DebugStoreLinkReporter(),
        autoAttachContent: ((String) -> Unit)? = null,
    ): HomeAttributionCoordinator {
        val parser = AttributionParser(
            actionBuilder = routing.actionBuilder,
            isExecutableAction = routing.isExecutableAction,
            contentIdFromAction = routing.contentIdFromAction,
            providerFieldExtractors = routing.providerFieldExtractors,
            regexFallback = routing.regexFallback,
        )
        val executor = AttributionExecutor(
            parser = parser,
            dispatcher = dispatcher,
            reporter = reporter,
            uploadTypeRegistry = UploadTypeRegistry(),
            autoAttachContent = autoAttachContent,
        )
        val facebookAppId = sdkConfig.facebookApplicationId()
        return HomeAttributionCoordinator(
            rawLinkCache = rawLinkCache(context),
            facade = AttributionFacade(
                runtimeProviders = buildList {
                    if (sdkConfig.clipboardEnabled) {
                        add(ClipboardAttributionProvider(context))
                    }
                    if (facebookAppId != null) {
                        add(FacebookDeferredAppLinkProvider(context, facebookAppId))
                    }
                    if (sdkConfig.isAppsFlyerEnabled()) {
                        add(AppsFlyerRuntimeProvider())
                    }
                },
                installProviders = buildList {
                    if (facebookAppId != null) {
                        add(FacebookInstallReferrerProvider(context))
                    }
                    add(GoogleInstallReferrerProvider(context))
                    if (sdkConfig.isAppsFlyerEnabled()) {
                        add(AppsFlyerInstallConversionProvider())
                    }
                },
            ),
            executor = executor,
        )
    }

    fun resolveSdk(rawValue: String): LinkSdkType {
        val uri = runCatching { Uri.parse(rawValue) }.getOrNull()
        val host = uri?.host.orEmpty().lowercase()
        return when {
            rawValue.contains("appsflyer", ignoreCase = true) -> LinkSdkType.APPSFLYER
            rawValue.contains("facebook", ignoreCase = true) -> LinkSdkType.FACEBOOK
            rawValue.contains("fbclid", ignoreCase = true) -> LinkSdkType.FACEBOOK
            uri?.scheme?.lowercase()?.startsWith("fb") == true -> LinkSdkType.FACEBOOK
            rawValue.contains("firebase", ignoreCase = true) -> LinkSdkType.FIREBASE
            rawValue.contains("engage", ignoreCase = true) -> LinkSdkType.GOOGLE_ENGAGE
            host == "u" || host == "universal" -> LinkSdkType.UNIVERSAL
            else -> LinkSdkType.NONE
        }
    }
}
