package com.example.simplewebview.attribution

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class AttributionParserFacebookTest {
    private val parser = AttributionParser(
        actionBuilder = { screen, contentId ->
            buildString {
                append("simplewebview://route?screen=")
                append(screen)
                if (!contentId.isNullOrBlank()) {
                    append("&contentId=")
                    append(contentId)
                }
            }
        },
        isExecutableAction = { raw -> raw.startsWith("simplewebview://route?") },
        contentIdFromAction = { raw ->
            Regex("contentId=([^&]+)").find(raw)?.groupValues?.get(1)
        },
    )

    @Test
    fun `parse facebook referrer target_url`() {
        val result = parser.parse(
            "utm_source=facebook&target_url=simplewebview%3A%2F%2Froute%3Fscreen%3Dbooking",
            LinkSdkType.FACEBOOK,
        )

        assertNotNull(result)
        assertEquals("simplewebview://route?screen=booking", result?.action)
    }

    @Test
    fun `parse facebook referrer applink_url openwith`() {
        val result = parser.parse(
            "utm_source=facebook&applink_url=https%3A%2F%2Flink.simplewebview.local%2Fopenwith%3Fbookid%3D88%26needOpen%3D1",
            LinkSdkType.FACEBOOK,
        )

        assertNotNull(result)
        assertEquals("88", result?.contentId)
        assertEquals("simplewebview://route?screen=yearly_plan&contentId=88", result?.action)
    }
}
