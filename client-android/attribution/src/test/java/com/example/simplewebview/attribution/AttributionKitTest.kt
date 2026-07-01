package com.example.simplewebview.attribution

import org.junit.Assert.assertEquals
import org.junit.Test

class AttributionKitTest {
    @Test
    fun `resolve sdk recognizes facebook and appsflyer markers`() {
        assertEquals(
            LinkSdkType.FACEBOOK,
            AttributionKit.resolveSdk("https://example.com/path?fbclid=abc"),
        )
        assertEquals(
            LinkSdkType.APPSFLYER,
            AttributionKit.resolveSdk("https://app.appsflyer.com/id123"),
        )
    }
}
