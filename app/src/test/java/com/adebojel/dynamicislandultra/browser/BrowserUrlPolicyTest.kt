package com.adebojel.dynamicislandultra.browser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserUrlPolicyTest {
    @Test fun upgradesHttpAndKeepsHttps() {
        assertEquals("https://example.com", BrowserUrlPolicy.resolve("http://example.com"))
        assertEquals("https://example.com", BrowserUrlPolicy.resolve("https://example.com"))
    }
    @Test fun searchQueryUsesDuckDuckGo() {
        assertTrue(BrowserUrlPolicy.resolve("open source tools")!!.startsWith("https://duckduckgo.com/?q="))
    }
    @Test fun rejectsNonWebSchemesForNavigation() {
        assertFalse(BrowserUrlPolicy.isAllowedWebScheme("javascript:alert(1)"))
        assertFalse(BrowserUrlPolicy.isAllowedWebScheme("file:///sdcard/test.html"))
        assertFalse(BrowserUrlPolicy.isAllowedWebScheme("content://provider/item"))
        assertTrue(BrowserUrlPolicy.isAllowedWebScheme("https://example.com"))
    }
}
