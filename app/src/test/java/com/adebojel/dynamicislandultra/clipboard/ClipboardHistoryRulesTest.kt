package com.adebojel.dynamicislandultra.clipboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClipboardHistoryRulesTest {
    @Test fun trimsAndRejectsEmptyClipboardText() {
        assertEquals("hello", ClipboardHistoryRules.cleanText("  hello  "))
        assertNull(ClipboardHistoryRules.cleanText(null))
        assertNull(ClipboardHistoryRules.cleanText("  "))
    }

    @Test fun capsCapturedTextLength() {
        val text = ClipboardHistoryRules.cleanText("x".repeat(ClipboardHistoryRules.MAX_TEXT_LENGTH + 20))
        assertEquals(ClipboardHistoryRules.MAX_TEXT_LENGTH, text?.length)
    }

    @Test fun declaresBoundedHistory() {
        assertTrue(ClipboardHistoryRules.MAX_ITEMS in 1..100)
    }
}
