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
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import android.content.Intent
import org.hamcrest.Matchers.* // Import for allOf, not

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

    @Test
    fun testSuccessfulTestRun() {
        // Enter a valid number of loops.
        composeTestRule.onNodeWithText("Number of Loops").performTextClearance() // Clear existing text
        composeTestRule.onNodeWithText("Number of Loops").performTextInput("3")

        // Click the "Start Test" button.
        composeTestRule.onNodeWithText("Start Test").performClick()

        // Wait for the test to complete (you might need to adjust the wait time).
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithText("DONE")
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Verify that the "DONE" label is displayed.
        composeTestRule.onNodeWithText("DONE").assertIsDisplayed()

        //check if "Loop: 3" is displayed
        composeTestRule.onNodeWithText("Loop: 3").assertIsDisplayed()
    }

}