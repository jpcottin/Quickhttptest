package com.example.quickhttptest

import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
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

class HttpTest(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun test(
        url: String,
        maxLoops: Int,
        bufferSize: Int,
        updateLoop: ((Int) -> Unit)?,
        logCallback: ((String) -> Unit)?,
        onLoopDone: ((Long) -> Unit)?
    ) = withContext(ioDispatcher) {
        val startTime = System.nanoTime()

        for (i in 1..maxLoops) {
            if (!isActive) {
                logCallback?.invoke("Test cancelled.")
                break
            }

            val message = "Loop $i"
            Log.d("HttpTest", message)
            updateLoop?.invoke(i)
            logCallback?.invoke(message)

            var conn: HttpURLConnection? = null
            try {
                conn = URL(url).openConnection() as HttpURLConnection
                conn.setRequestProperty("Connection", "close")
                conn.connectTimeout = 5000
                conn.readTimeout = 10000

                val status = conn.responseCode
                val isErrorStatus = status >= 400

                if (isErrorStatus) {
                    val statusMsg = "HTTP Error $status: ${conn.responseMessage}"
                    Log.e("HttpTest", statusMsg)
                    logCallback?.invoke(statusMsg)
                }

                val inputStream = if (isErrorStatus) conn.errorStream else conn.inputStream

                if (inputStream != null) {
                    val buffer = ByteArray(bufferSize)
                    try {
                        while (true) {
                            yield()

                            val bytesRead = inputStream.read(buffer)
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

            } catch (e: CancellationException) {
                throw e
            } catch (e: SocketTimeoutException) {
                val errorMessage = "Timeout Error: ${e.message}"
                Log.e("HttpTest", errorMessage, e)
                logCallback?.invoke(errorMessage)
                break
            } catch (e: ConnectException) {
                val errorMessage = "Connection Error: ${e.message}"
                Log.e("HttpTest", errorMessage, e)
                logCallback?.invoke(errorMessage)
                break
            } catch (e: UnknownHostException) {
                val errorMessage = "Unknown Host: ${e.message}"
                Log.e("HttpTest", errorMessage, e)
                logCallback?.invoke(errorMessage)
                break
            } catch (e: IOException) {
                val errorMessage = "IO Error: ${e.message}"
                Log.e("HttpTest", errorMessage, e)
                logCallback?.invoke(errorMessage)
                break
            } catch (e: RuntimeException) {
                val errorMessage = "Runtime Error: ${e.message}"
                Log.e("HttpTest", errorMessage, e)
                logCallback?.invoke(errorMessage)
                break
            } finally {
                conn?.disconnect()
            }
        }

        if (isActive) {
            val endTime = System.nanoTime()
            val elapsedTimeMs = (endTime - startTime) / 1_000_000
            onLoopDone?.invoke(elapsedTimeMs)
        }
    }
}
