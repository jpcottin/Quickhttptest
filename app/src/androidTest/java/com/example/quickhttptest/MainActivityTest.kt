package com.example.quickhttptest

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput


@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testAppStartup() {
        // Verify that the "HTTP test" title is displayed.
        composeTestRule.onNodeWithText("HTTP test").assertIsDisplayed()

        // Verify that the "Start Test" button is displayed.
        composeTestRule.onNodeWithText("Start Test").assertIsDisplayed()
    }


    @Test
    fun testInputError() {
        // Clear the input field.
        composeTestRule.onNodeWithText("Number of Loops").performTextClearance()
        composeTestRule.onNodeWithText("Number of Loops").performTextInput("")

        // Click the "Start Test" button.
        composeTestRule.onNodeWithText("Start Test").performClick()

        // Verify that the error message is displayed.
        composeTestRule.onNodeWithText("Please enter a positive number").assertIsDisplayed()

        // Enter invalid input.
        composeTestRule.onNodeWithText("Number of Loops").performTextClearance() // Clear first!
        composeTestRule.onNodeWithText("Number of Loops").performTextInput("-5")
        composeTestRule.onNodeWithText("Start Test").performClick()
        composeTestRule.onNodeWithText("Please enter a positive number").assertIsDisplayed()

        composeTestRule.onNodeWithText("Number of Loops").performTextClearance() // Clear first!
        composeTestRule.onNodeWithText("Number of Loops").performTextInput("abc")
        composeTestRule.onNodeWithText("Start Test").performClick()
        composeTestRule.onNodeWithText("Please enter a positive number").assertIsDisplayed()

    }

}
