package com.adebojel.dynamicislandultra.ai

import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class WicaraComposeUiTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<OfflineAssistantActivity>()

    @Test
    fun assistantScreenShowsExplicitOfflineAiControls() {
        composeRule.onNodeWithText("Aktifkan AI").assertIsDisplayed()
        composeRule.onNodeWithText("Batalkan").assertExists()
        composeRule.onNodeWithText("Nonaktifkan AI").assertDoesNotExist()
    }
}
