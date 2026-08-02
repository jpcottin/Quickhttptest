package com.example.quickhttptest

import android.util.Log
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HttpTestTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val httpTest = HttpTest(ioDispatcher = testDispatcher)
    private lateinit var server: MockWebServer

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        server = MockWebServer()
        server.start()
    }

    @After
    fun teardown() {
        server.shutdown()
        unmockkAll()
    }

    @Test
    fun testHttpTestCompletesAllLoops() = runTest(testDispatcher) {
        repeat(2) { server.enqueue(MockResponse().setBody("payload for one loop")) }

        var loopCount = 0
        val logs = mutableListOf<String>()
        var result: HttpTestResult? = null

        httpTest.test(
            url = server.url("/100.txt").toString(),
            maxLoops = 2,
            bufferSize = 8192,
            updateLoop = { loopCount = it },
            logCallback = { logs.add(it) },
            onLoopDone = { result = it }
        )

        assertEquals(2, server.requestCount, "Each loop should issue one request")
        assertEquals(2, loopCount)
        assertTrue(logs.any { it.startsWith("Read ") }, "Should have logged payload reads")

        val done = assertNotNull(result, "onLoopDone should have been called")
        assertTrue(done.success)
        assertEquals(2, done.completedLoops)
        assertTrue(done.elapsedTimeMs >= 0, "Elapsed time should be calculated")
    }

    @Test
    fun testHttpTestLogsHttpErrorStatusAndContinues() = runTest(testDispatcher) {
        server.enqueue(MockResponse().setResponseCode(404).setBody("not found"))
        server.enqueue(MockResponse().setBody("ok"))

        val logs = mutableListOf<String>()
        var result: HttpTestResult? = null

        httpTest.test(
            url = server.url("/missing.txt").toString(),
            maxLoops = 2,
            bufferSize = 8192,
            updateLoop = {},
            logCallback = { logs.add(it) },
            onLoopDone = { result = it }
        )

        assertTrue(logs.any { it.contains("HTTP Error 404") }, "Should have logged the HTTP error status")

        val done = assertNotNull(result, "onLoopDone should have been called")
        assertEquals(2, done.completedLoops, "An HTTP error status should not stop the remaining loops")
    }

    @Test
    fun testHttpTestStopsAfterFirstError() = runTest(testDispatcher) {
        // Shut the server down so the URL points at a closed local port.
        val unreachableUrl = server.url("/100.txt").toString()
        server.shutdown()

        var loopCount = 0
        val logs = mutableListOf<String>()
        var result: HttpTestResult? = null

        httpTest.test(
            url = unreachableUrl,
            maxLoops = 5,
            bufferSize = 8192,
            updateLoop = { loopCount = it },
            logCallback = { logs.add(it) },
            onLoopDone = { result = it }
        )

        assertEquals(1, loopCount, "Should stop after first error")
        assertTrue(logs.any { it.contains("Error") }, "Should have logged an error")

        val done = assertNotNull(result, "onLoopDone should have been called")
        assertFalse(done.success)
        assertEquals(0, done.completedLoops)
    }
}
