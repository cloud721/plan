package com.example.simplewebview.attribution

class AttributionExecutor(
    private val parser: LinkParser,
    private val dispatcher: LinkDispatcher,
    private val reporter: LinkReporter,
    private val uploadTypeRegistry: UploadTypeRegistry,
    private val autoAttachContent: ((String) -> Unit)? = null,
) {
    private var hasDoneActionFromLink: Boolean = false

    fun consumeCachedIntent(
        rawValue: String,
        sdk: LinkSdkType,
        launchType: LaunchType,
    ): LinkExecutionResult {
        AttributionDebugStore.add("executor", "consumeCachedIntent sdk=$sdk raw=$rawValue")
        val startedAt = System.currentTimeMillis()
        val result = parser.parse(rawValue, sdk)
        return execute(
            rawValue = rawValue,
            parsed = result,
            uploadType = uploadTypeRegistry.resolve(
                sdk = sdk,
                fromInstallRef = false,
                fromIntent = true,
            ),
            launchType = launchType,
            startedAt = startedAt,
        )
    }

    fun consumeProviderResult(
        data: AppLinkData,
        launchType: LaunchType,
        fromInstallRef: Boolean = false,
        fromIntent: Boolean = false,
    ): LinkExecutionResult {
        AttributionDebugStore.add(
            "executor",
            "consumeProviderResult sdk=${data.sdk} fromInstallRef=$fromInstallRef fromIntent=$fromIntent raw=${data.linkUrl ?: data.ref.orEmpty()}",
        )
        val startedAt = System.currentTimeMillis()
        val result = parser.parse(data)
        val rawValue = when {
            fromInstallRef && !data.ref.isNullOrBlank() -> data.ref
            !data.linkUrl.isNullOrBlank() -> data.linkUrl
            else -> data.ref.orEmpty()
        }
        return execute(
            rawValue = rawValue,
            parsed = result,
            uploadType = uploadTypeRegistry.resolve(
                sdk = data.sdk,
                fromInstallRef = fromInstallRef,
                fromIntent = fromIntent,
            ),
            launchType = launchType,
            startedAt = startedAt,
        )
    }

    private fun execute(
        rawValue: String,
        parsed: AnalyticsResult?,
        uploadType: Int,
        launchType: LaunchType,
        startedAt: Long,
    ): LinkExecutionResult {
        AttributionDebugStore.add(
            "executor",
            "execute parsedAction=${parsed?.action} contentId=${parsed?.contentId} uploadType=$uploadType launchType=$launchType",
        )
        val opened = if (!hasDoneActionFromLink) {
            executeInternal(parsed)
        } else {
            false
        }
        parsed?.contentId?.let { contentId ->
            if (opened || uploadType != UploadTypeRegistry.UPLOAD_SDK_CHANGDU_DEFAULT) {
                autoAttachContent?.invoke(contentId)
            }
        }
        if (opened) {
            hasDoneActionFromLink = true
        }
        reporter.upload(
            rawValue = rawValue,
            result = parsed,
            launchType = launchType,
            uploadType = uploadType,
            opened = opened,
            durationMs = System.currentTimeMillis() - startedAt,
        )
        return LinkExecutionResult(
            opened = opened,
            uploadType = uploadType,
            contentId = parsed?.contentId,
            action = parsed?.action,
        )
    }

    private fun executeInternal(parsed: AnalyticsResult?): Boolean {
        if (parsed == null) return false
        val action = parsed.action ?: return false
        if (parsed.fromOpenWith && !parsed.needOpen) {
            return true
        }
        return dispatcher.dispatch(action)
    }

    fun resetLaunchCycle() {
        hasDoneActionFromLink = false
        AttributionDebugStore.add("executor", "reset launch cycle")
    }
}
