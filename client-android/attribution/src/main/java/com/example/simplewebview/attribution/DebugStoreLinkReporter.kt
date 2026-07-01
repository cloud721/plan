package com.example.simplewebview.attribution

import android.util.Log

class DebugStoreLinkReporter(
    private val tag: String = "AttributionKit",
) : LinkReporter {
    override fun upload(
        rawValue: String,
        result: AnalyticsResult?,
        launchType: LaunchType,
        uploadType: Int,
        opened: Boolean,
        durationMs: Long,
    ) {
        AttributionDebugStore.add(
            "report",
            "raw=$rawValue uploadType=$uploadType launchType=$launchType opened=$opened action=${result?.action} contentId=${result?.contentId} durationMs=$durationMs",
        )
        Log.d(
            tag,
            "upload raw=$rawValue launchType=$launchType uploadType=$uploadType opened=$opened durationMs=$durationMs action=${result?.action} contentId=${result?.contentId}",
        )
    }
}
