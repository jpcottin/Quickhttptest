package com.example.quickhttptest

import android.util.Log
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue

class HttpTestTest {

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun testHttpTestInvalidUrl() = runTest {
        var loopCount = 0
        val logs = mutableListOf<String>()
        var elapsed = -1L

        HttpTest.test(
            url = "http://invalid.url.that.does.not.exist",
            maxLoops = 2,
            bufferSize = 8192,
            updateLoop = { loopCount = it },
            logCallback = { logs.add(it) },
            onLoopDone = { elapsed = it }
        )

        assertTrue(loopCount > 0, "Loop should have started")
        assertTrue(elapsed >= 0, "Elapsed time should be calculated")
        assertTrue(logs.any { it.contains("Unknown Host") || it.contains("Error") }, "Should have logged an error")
    }
}