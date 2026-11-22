package com.adebojel.dynamicislandultra.reminders

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderRulesTest {
    @Test fun acceptsSupportedDurations() {
        assertTrue(ReminderRules.isValidMinutes(1))
        assertTrue(ReminderRules.isValidMinutes(10_080))
        assertFalse(ReminderRules.isValidMinutes(0))
        assertFalse(ReminderRules.isValidMinutes(10_081))
    }
    @Test fun validatesReminderTitleLength() {
        assertTrue(ReminderRules.isValidTitle("Meeting"))
        assertFalse(ReminderRules.isValidTitle(" "))
        assertFalse(ReminderRules.isValidTitle("x".repeat(81)))
    }
}
