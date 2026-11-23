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
    @Test fun sessionRestoreKeepsHttpsTabsAndCapsTabCount() {
        val restored = BrowserSessionPolicy.restoreableUrls(
            listOf("https://one.example", "http://two.example", "https://three.example"),
            maxTabs = 2
        )
        assertEquals(listOf("https://one.example", "https://three.example"), restored)
    }

    @Test fun activeTabIndexIsAlwaysInRange() {
        assertEquals(0, BrowserSessionPolicy.safeActiveIndex(-2, 3))
        assertEquals(2, BrowserSessionPolicy.safeActiveIndex(8, 3))
        assertEquals(0, BrowserSessionPolicy.safeActiveIndex(8, 0))
    }

    @Test fun rejectsNonWebSchemesForNavigation() {
        assertFalse(BrowserUrlPolicy.isAllowedWebScheme("javascript:alert(1)"))
        assertFalse(BrowserUrlPolicy.isAllowedWebScheme("file:///sdcard/test.html"))
        assertFalse(BrowserUrlPolicy.isAllowedWebScheme("content://provider/item"))
        assertTrue(BrowserUrlPolicy.isAllowedWebScheme("https://example.com"))
    }
}
