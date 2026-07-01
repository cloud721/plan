package com.example.simplewebview.attribution

import org.junit.Assert.assertEquals
import org.junit.Test

class UploadTypeRegistryTest {
    private val registry = UploadTypeRegistry()

    @Test
    fun `facebook intent uses facebook deep link bucket`() {
        assertEquals(
            UploadTypeRegistry.UPLOAD_SDK_FACEBOOK_DEEP_LINK,
            registry.resolve(LinkSdkType.FACEBOOK, fromInstallRef = false, fromIntent = true),
        )
    }

    @Test
    fun `facebook install referrer uses facebook install bucket`() {
        assertEquals(
            UploadTypeRegistry.UPLOAD_SDK_FACEBOOK_INSTALL,
            registry.resolve(LinkSdkType.FACEBOOK, fromInstallRef = true, fromIntent = false),
        )
    }
}
