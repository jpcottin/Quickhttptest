package com.example.quickhttptest

import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.io.IOException
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException
import kotlin.math.max
import kotlin.math.min

data class HttpTestResult(
    val elapsedTimeMs: Long,
    val completedLoops: Int,
    val success: Boolean,
    val httpErrorCount: Int = 0
)

class HttpTest(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun test(
        url: String,
        maxLoops: Int,
        bufferSize: Int,
        updateLoop: ((Int) -> Unit)?,
        logCallback: ((String) -> Unit)?,
        onLoopDone: ((HttpTestResult) -> Unit)?
    ) = withContext(ioDispatcher) {
        val startTime = System.nanoTime()
        var completedLoops = 0
        var failed = false
        var httpErrorCount = 0

        for (i in 1..maxLoops) {
            ensureActive()

            val message = "Loop $i"
            Log.d("HttpTest", message)
            updateLoop?.invoke(i)
            logCallback?.invoke(message)

            var conn: HttpURLConnection? = null
            try {
                conn = URL(url).openConnection() as HttpURLConnection
                val connection: HttpURLConnection = conn
                coroutineScope {
                    // HttpURLConnection blocks and ignores coroutine cancellation, so a
                    // stalled server would keep the run alive until its timeouts expire.
                    // Disconnecting from the watchdog makes the blocked call throw at once.
                    val watchdog = launch(start = CoroutineStart.UNDISPATCHED) {
                        try {
                            awaitCancellation()
                        } finally {
                            connection.disconnect()
                        }
                    }
                    connection.setRequestProperty("Connection", "close")
                    connection.connectTimeout = 5000
                    connection.readTimeout = 10000

                    val status = connection.responseCode
                    val isErrorStatus = status >= 400

                    if (isErrorStatus) {
                        httpErrorCount++
                        val statusMsg = "HTTP Error $status: ${connection.responseMessage}"
                        Log.e("HttpTest", statusMsg)
                        logCallback?.invoke(statusMsg)
                    }

                    val inputStream = if (isErrorStatus) connection.errorStream else connection.inputStream

                    if (inputStream != null) {
                        val buffer = ByteArray(bufferSize)
                        try {
                            while (true) {
                                yield()

                                val bytesRead = inputStream.read(buffer)
                                ensureActive()
                                if (bytesRead == -1) break

                                val logBytes = 25
                                val startStr = String(buffer, 0, min(logBytes, bytesRead))
                                val endStr = String(buffer, max(0, bytesRead - logBytes), min(logBytes, bytesRead))

                                val reading = "Read $bytesRead bytes: $startStr ... $endStr"
                                Log.d("HttpTest", reading)
                                logCallback?.invoke(reading)
                            }
                        } finally {
                            inputStream.close()
                        }
                    } else if (isErrorStatus) {
                        logCallback?.invoke("No error body provided by server.")
                    }
                    watchdog.cancel()
                }

                completedLoops = i

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // A disconnect triggered by cancellation surfaces here as an IOException:
                // report it as a cancellation rather than as a failed request.
                ensureActive()
                val label = when (e) {
                    is SocketTimeoutException -> "Timeout Error"
                    is ConnectException -> "Connection Error"
                    is UnknownHostException -> "Unknown Host"
                    is IOException -> "IO Error"
                    else -> "Runtime Error"
                }
                val errorMessage = "$label: ${e.message}"
                Log.e("HttpTest", errorMessage, e)
                logCallback?.invoke(errorMessage)
                failed = true
                break
            } finally {
                conn?.disconnect()
            }
        }

        ensureActive()
        val endTime = System.nanoTime()
        val elapsedTimeMs = (endTime - startTime) / 1_000_000
        onLoopDone?.invoke(HttpTestResult(elapsedTimeMs, completedLoops, !failed, httpErrorCount))
    }
}
