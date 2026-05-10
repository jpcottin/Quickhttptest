package com.example.quickhttptest

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextClearance
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.quickhttptest.ui.theme.QuickhttptestTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testInitialStateAndInteractions() {
        composeTestRule.setContent {
            QuickhttptestTheme {
                MainScreen()
            }
        }

        // Check if elements exist
        composeTestRule.onNodeWithText("HTTP test").assertExists()
        composeTestRule.onNodeWithText("Start Test").assertExists()

        // Select Local URL radio button
        composeTestRule.onNodeWithTag("radioButton_Local URL").performClick()

        // Change loops number
        composeTestRule.onNodeWithText("Number of Loops").performTextClearance()
        composeTestRule.onNodeWithText("Number of Loops").performTextInput("10")

        // Change buffer size
        composeTestRule.onNodeWithText("Buffer Size (bytes)").performTextClearance()
        composeTestRule.onNodeWithText("Buffer Size (bytes)").performTextInput("2048")

        // Check if LoopLabel is visible and displays default value
        composeTestRule.onNodeWithText("Loop: 0").assertExists()

        // Start Test (should interact)
        composeTestRule.onNodeWithText("Start Test").performClick()
    }
}
