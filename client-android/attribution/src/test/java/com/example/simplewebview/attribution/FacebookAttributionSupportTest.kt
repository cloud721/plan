package com.example.simplewebview.attribution

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FacebookAttributionSupportTest {
    @Test
    fun `normalize application id strips fb prefix`() {
        assertEquals("123456789", FacebookAttributionSupport.normalizeApplicationId("fb123456789"))
        assertEquals("123456789", FacebookAttributionSupport.normalizeApplicationId("123456789"))
    }

    @Test
    fun `facebook install referrer is detected from raw referrer`() {
        assertTrue(
            FacebookAttributionSupport.isFacebookInstallReferrer(
                "utm_source=facebook&link=simplewebview%3A%2F%2Froute%3Fscreen%3Dactivity"
            )
        )
        assertTrue(FacebookAttributionSupport.isFacebookInstallReferrer("fb4a_referrer=something"))
        assertFalse(
            FacebookAttributionSupport.isFacebookInstallReferrer(
                "utm_source=google-play&utm_campaign=spring"
            )
        )
    }
}
