package com.example.simplewebview.attribution

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClipboardAttributionSupportTest {
    @Test
    fun `normalize trims surrounding whitespace`() {
        assertEquals(
            "simplewebview://route?screen=activity",
            ClipboardAttributionSupport.normalize("  simplewebview://route?screen=activity  "),
        )
    }

    @Test
    fun `likely attribution payload supports deep links and referrer-style queries`() {
        assertTrue(ClipboardAttributionSupport.isLikelyAttributionPayload("simplewebview://booking"))
        assertTrue(
            ClipboardAttributionSupport.isLikelyAttributionPayload(
                "utm_source=facebook&applink_url=https%3A%2F%2Flink.simplewebview.local%2Fopenwith%3Fbookid%3D88%26needOpen%3D1"
            )
        )
    }

    @Test
    fun `likely attribution payload rejects ordinary clipboard text`() {
        assertFalse(ClipboardAttributionSupport.isLikelyAttributionPayload("hello world"))
        assertFalse(ClipboardAttributionSupport.isLikelyAttributionPayload("会议纪要"))
    }

    @Test
    fun `should consume requires payload to be non blank likely and not duplicate`() {
        assertTrue(
            ClipboardAttributionSupport.shouldConsume(
                rawValue = "simplewebview://route?screen=booking",
                lastConsumed = null,
            )
        )
        assertFalse(
            ClipboardAttributionSupport.shouldConsume(
                rawValue = "simplewebview://route?screen=booking",
                lastConsumed = "simplewebview://route?screen=booking",
            )
        )
        assertFalse(
            ClipboardAttributionSupport.shouldConsume(
                rawValue = "notes",
                lastConsumed = null,
            )
        )
    }
}
