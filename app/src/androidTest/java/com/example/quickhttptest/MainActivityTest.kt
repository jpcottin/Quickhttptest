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

import androidx.compose.ui.semantics.SemanticsProperties // Import SemanticsProperties

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
                .onAllNodesWithText("DONE", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Get the text of the DoneLabel.  This is the key addition.
        val doneLabelText = composeTestRule.onNodeWithText("DONE", substring = true)
            .fetchSemanticsNode()
            .config[SemanticsProperties.Text] // Correct way to get the text
            .first()
            .text


        // 1. Check if "ms" is present.
        assert(doneLabelText.contains("ms")) { "Done label should contain 'ms'" }


        // 2. Check if there's a positive number before "ms".
        val regex = Regex("""(\d+)\s*ms""") // Regular expression to match one or more digits followed by "ms"
        val matchResult = regex.find(doneLabelText)

        assert(matchResult != null) { "Done label should contain a number followed by 'ms'" }

        matchResult?.let {
            val numberString = it.groupValues[1] // Get the captured number (group 1)
            val number = numberString.toIntOrNull()
            assert(number != null) { "The matched group should be a number" } // Check for conversion errors
            assert(number!! >= 0) { "Elapsed time should be non-negative" } // Check for positive number
        }


        //check if "Loop: 3" is displayed
        composeTestRule.onNodeWithText("Loop: 3").assertIsDisplayed()
    }


}