package com.example.simplewebview.attribution.providers

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import com.example.simplewebview.attribution.AppLinkData
import com.example.simplewebview.attribution.AttributionDebugStore
import com.example.simplewebview.attribution.ClipboardAttributionSupport
import com.example.simplewebview.attribution.LinkSdkType
import com.example.simplewebview.attribution.RuntimeLinkProvider

class ClipboardAttributionProvider(
    context: Context,
) : RuntimeLinkProvider {
    private val appContext = context.applicationContext
    private val clipboardManager = appContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun handleAppLink(onResult: (AppLinkData) -> Unit) {
        val rawValue = readClipboardText()
        val lastConsumed = prefs.getString(KEY_LAST_CONSUMED, null)
        if (!ClipboardAttributionSupport.shouldConsume(rawValue, lastConsumed)) {
            AttributionDebugStore.add("clipboard", "skip raw=${rawValue.orEmpty()} lastConsumed=${lastConsumed.orEmpty()}")
            return
        }

        val normalized = ClipboardAttributionSupport.normalize(rawValue) ?: return
        AttributionDebugStore.add("clipboard", "consume raw=$normalized")
        prefs.edit().putString(KEY_LAST_CONSUMED, normalized).apply()
        onResult(
            AppLinkData(
                sdk = LinkSdkType.CLIPBOARD,
                linkUrl = normalized,
                ref = normalized,
                extras = mapOf("source" to "clipboard"),
            )
        )
    }

    private fun readClipboardText(): String? {
        val description = clipboardManager.primaryClipDescription ?: return null
        if (!description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) &&
            !description.hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML)
        ) {
            return null
        }
        val clipData = clipboardManager.primaryClip ?: return null
        if (clipData.itemCount <= 0) return null
        return clipData.getItemAt(0).coerceToText(appContext)?.toString()
    }

    private companion object {
        const val PREFS_NAME = "clipboard_attribution"
        const val KEY_LAST_CONSUMED = "last_consumed"
    }
}
