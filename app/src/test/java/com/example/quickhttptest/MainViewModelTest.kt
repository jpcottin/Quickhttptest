package com.example.quickhttptest

import android.util.Log
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

// Runs real requests on Dispatchers.IO against a local MockWebServer, so waits
// use real-time timeouts rather than virtual time.
@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private lateinit var server: MockWebServer
    private lateinit var viewModel: MainViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        server = MockWebServer()
        server.start()
        val url = server.url("/100.txt").toString()
        viewModel = MainViewModel(distantUrl = url, localUrl = url)
    }

    @After
    fun teardown() {
        server.shutdown()
        unmockkAll()
        Dispatchers.resetMain()
    }

    private suspend fun awaitNotRunning() = withTimeout(5000) {
        viewModel.uiState.first { !it.isRunning }
    }

    @Test
    fun httpErrorStatusesAreCountedInTheResult() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(500).setBody("boom"))
        server.enqueue(MockResponse().setBody("ok"))
        viewModel.onLoopInputChanged("2")

        viewModel.toggleTest()
        val state = awaitNotRunning()

        assertTrue(state.isLoopDone)
        assertTrue(state.isSuccess)
        assertEquals(2, state.loopValue)
        assertEquals(1, state.httpErrorCount)
    }

    @Test
    fun stopEndsAStalledRunPromptlyAndAllowsARestart() = runBlocking {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
        viewModel.onLoopInputChanged("1")

        viewModel.toggleTest()
        assertTrue(viewModel.uiState.value.isRunning)
        assertNotNull(server.takeRequest(5, TimeUnit.SECONDS), "The request should reach the server")

        viewModel.toggleTest()
        // Well under the 10 s read timeout the stalled request would otherwise wait for.
        val stopped = withTimeout(2000) { viewModel.uiState.first { !it.isRunning } }
        assertFalse(stopped.isLoopDone, "A stopped run should not report a result")

        server.enqueue(MockResponse().setBody("ok"))
        viewModel.toggleTest()
        val restarted = awaitNotRunning()

        assertTrue(restarted.isLoopDone)
        assertTrue(restarted.isSuccess)
        assertEquals(0, restarted.httpErrorCount)
        assertFalse(
            restarted.logMessages.any { it.contains("Error") },
            "The stopped run must not leak messages into the new one: ${restarted.logMessages}"
        )
    }
}
