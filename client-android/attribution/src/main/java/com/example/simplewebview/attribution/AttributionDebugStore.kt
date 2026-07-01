package com.example.simplewebview.attribution

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AttributionDebugEntry(
    val timestamp: Long,
    val stage: String,
    val message: String,
)

object AttributionDebugStore {
    private const val MAX_ENTRIES = 120
    private val entries = mutableListOf<AttributionDebugEntry>()
    private val formatter = SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US)

    @Synchronized
    fun add(stage: String, message: String) {
        entries.add(
            AttributionDebugEntry(
                timestamp = System.currentTimeMillis(),
                stage = stage,
                message = message,
            )
        )
        while (entries.size > MAX_ENTRIES) {
            entries.removeAt(0)
        }
    }

    @Synchronized
    fun clear() {
        entries.clear()
    }

    @Synchronized
    fun render(): String {
        if (entries.isEmpty()) {
            return "No attribution events yet."
        }
        return buildString {
            entries.forEach { entry ->
                append(formatter.format(Date(entry.timestamp)))
                append("  [")
                append(entry.stage)
                append("] ")
                append(entry.message)
                append('\n')
            }
        }.trimEnd()
    }
}
