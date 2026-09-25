package com.example.quickhttptest

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.quickhttptest.ui.theme.QuickhttptestTheme
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-end UI tests for the HTTP test flow, backed by an on-device MockWebServer
 * so they are deterministic and never depend on external network conditions.
 */
@RunWith(AndroidJUnit4::class)
class MainScreenHttpFlowTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var server: MockWebServer

    @Before
    fun setup() {
        server = MockWebServer()
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse =
                MockResponse().setBody("x".repeat(100))
        }
        server.start()
    }

    @After
    fun teardown() {
        server.shutdown()
    }

    private fun setMainScreenContent() {
        val url = server.url("/100.txt").toString()
        composeTestRule.setContent {
            QuickhttptestTheme {
                MainScreen(viewModel = MainViewModel(distantUrl = url, localUrl = url))
            }
        }
    }

    private fun runTestAndAwaitDone(numberOfLoops: Int) {
        composeTestRule.onNodeWithText("Number of Loops").performTextClearance()
        composeTestRule.onNodeWithText("Number of Loops").performTextInput(numberOfLoops.toString())

        composeTestRule.onNodeWithText("Start Test").performClick()

        // Wait until the run finished successfully AND the loop counter shows the
        // final value, so a stale "DONE" from a previous run can't satisfy the wait.
        // Also wait for the button to flip back, which happens once the job has ended.
        composeTestRule.waitUntil(timeoutMillis = 30000) {
            composeTestRule.onAllNodesWithText("DONE in", substring = true)
                .fetchSemanticsNodes().isNotEmpty() &&
                composeTestRule.onAllNodesWithText("Loop: $numberOfLoops")
                    .fetchSemanticsNodes().isNotEmpty() &&
                composeTestRule.onAllNodesWithText("Start Test")
                    .fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun assertDoneLabelReportsElapsedTime() {
        val doneLabelText = composeTestRule.onNodeWithText("DONE in", substring = true)
            .fetchSemanticsNode()
            .config[SemanticsProperties.Text]
            .first()
            .text

        assert(doneLabelText.contains("ms")) { "Done label should contain 'ms'" }

        val matchResult = Regex("""(\d+)\s*ms""").find(doneLabelText)
        assert(matchResult != null) { "Done label should contain a number followed by 'ms'" }

        val number = matchResult!!.groupValues[1].toIntOrNull()
        assert(number != null) { "The matched group should be a number" }
        assert(number!! >= 0) { "Elapsed time should be non-negative" }
    }

    @Test
    fun testSuccessfulTestRun() {
        setMainScreenContent()

        runTestAndAwaitDone(numberOfLoops = 3)
        assertDoneLabelReportsElapsedTime()

        composeTestRule.onNodeWithText("Loop: 3").assertExists()
        assert(server.requestCount == 3) { "Each loop should issue one request" }
    }

    @Test
    fun testDifferentNumberOfLoops() {
        setMainScreenContent()

        val listOfLoops = listOf(1, 3, 5)
        for (numberOfLoops in listOfLoops) {
            runTestAndAwaitDone(numberOfLoops)
            assertDoneLabelReportsElapsedTime()
            composeTestRule.onNodeWithText("Loop: $numberOfLoops").assertExists()
        }

        assert(server.requestCount == listOfLoops.sum()) { "Each loop should issue one request" }
    }
}
