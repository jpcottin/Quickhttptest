package com.example.quickhttptest

import android.util.Log
import kotlinx.coroutines.isActive
import kotlinx.coroutines.yield
import java.io.IOException
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException
import kotlin.coroutines.coroutineContext
import kotlin.math.max
import kotlin.math.min

object HttpTest {
    suspend fun test(
        url: String,
        maxLoops: Int,
        bufferSize: Int,
        updateLoop: ((Int) -> Unit)?,
        logCallback: ((String) -> Unit)?,
        onLoopDone: ((Long) -> Unit)? // Accept elapsed time (Long)
    ) {
        val startTime = System.nanoTime() // Capture start time

        for (i in 1..maxLoops) {
            // Check if the coroutine was cancelled between loops
            if (!coroutineContext.isActive) {
                logCallback?.invoke("Test cancelled.")
                break
            }

            val message = "Loop $i"
            Log.d("NetworkBug", message)
            updateLoop?.invoke(i)
            logCallback?.invoke(message)

            var conn: HttpURLConnection? = null
            try {
                conn = URL(url).openConnection() as HttpURLConnection
                conn.setRequestProperty("Connection", "close")
                conn.connectTimeout = 5000 // 5 seconds connect timeout
                conn.readTimeout = 10000 // 10 seconds read timeout
                
                val status = conn.responseCode
                val isErrorStatus = status >= 400
                
                if (isErrorStatus) {
                    val statusMsg = "HTTP Error $status: ${conn.responseMessage}"
                    Log.e("NetworkBug", statusMsg)
                    logCallback?.invoke(statusMsg)
                }

                // If error, read from errorStream, otherwise from inputStream
                val `is` = if (isErrorStatus) conn.errorStream else conn.inputStream
                
                if (`is` != null) {
                    val buffer = ByteArray(bufferSize)
                    var totalBytesRead = 0

                    while (true) {
                        yield() // Yield to allow cancellation during tight reading loops
                        
                        val bytesRead = `is`.read(buffer)
                        if (bytesRead == -1) {
                            break
                        }
                        totalBytesRead += bytesRead
                        
                        val LOG_BYTES = 25
                        val startStr = String(buffer, 0, min(LOG_BYTES, bytesRead))
                        val endStr = String(buffer, max(0, bytesRead - LOG_BYTES), min(LOG_BYTES, bytesRead))
                        
                        val reading = "Read $bytesRead bytes: $startStr ... $endStr"
                        Log.d("NetworkBug", reading)
                        logCallback?.invoke(reading)
                    }
                    `is`.close()
                } else if (isErrorStatus) {
                    logCallback?.invoke("No error body provided by server.")
                }
                
                // If it was an HTTP error, we might still want to break the loop or continue
                // For this test tool, we'll continue to the next loop unless it's a hard network error.

            } catch (e: SocketTimeoutException) {
                val errorMessage = "Timeout Error: ${e.message}"
                Log.e("NetworkBug", errorMessage, e)
                logCallback?.invoke(errorMessage)
                break // Exit the loop on timeout
            } catch (e: ConnectException) {
                val errorMessage = "Connection Error: ${e.message}"
                Log.e("NetworkBug", errorMessage, e)
                logCallback?.invoke(errorMessage)
                break // Exit the loop on connection refusal
            } catch (e: UnknownHostException) {
                val errorMessage = "Unknown Host: ${e.message}"
                Log.e("NetworkBug", errorMessage, e)
                logCallback?.invoke(errorMessage)
                break
            } catch (e: IOException) {
                val errorMessage = "IO Error: ${e.message}"
                Log.e("NetworkBug", errorMessage, e)
                logCallback?.invoke(errorMessage)
                break
            } catch (e: RuntimeException) {
                val errorMessage = "Runtime Error: ${e.message}"
                Log.e("NetworkBug", errorMessage, e)
                logCallback?.invoke(errorMessage)
                break
            } finally {
                conn?.disconnect()
            }
        }
        val endTime = System.nanoTime() // Capture end time
        val elapsedTimeMs = (endTime - startTime) / 1_000_000 // Calculate elapsed time in milliseconds
        onLoopDone?.invoke(elapsedTimeMs) // Pass elapsed time to callback
    }
}