package com.example.quickhttptest

import android.util.Log
import java.io.IOException
import java.net.ConnectException
import java.net.URL
import java.net.UnknownHostException
import kotlin.math.max
import kotlin.math.min

object HttpTest {
    fun test(
        url: String,
        maxLoops: Int,
        updateLoop: ((Int) -> Unit)?,
        logCallback: ((String) -> Unit)?,
        onLoopDone: ((Long) -> Unit)? // Accept elapsed time (Long)
    ) {
        val startTime = System.nanoTime() // Capture start time

        for (i in 0..maxLoops) {
            val message = "Loop $i"
            Log.d("NetworkBug", message)
            updateLoop?.invoke(i)
            logCallback?.invoke(message)

            try {
                val conn = URL(url).openConnection()
                conn.setRequestProperty("Connection", "close")
                val `is` = conn.getInputStream()

                var contentLength = conn.contentLength
                if (contentLength == -1) {
                    contentLength = 100000
                }

                val buffer = ByteArray(contentLength)

                var offset = 0
                while (offset < buffer.size) {
                    val bytesLeft = buffer.size - offset
                    val requesting = "Requesting $bytesLeft bytes"
                    Log.d("NetworkBug", requesting)
                    logCallback?.invoke(requesting)
                    val bytesRead = `is`.read(buffer, offset, bytesLeft)
                    if (bytesRead == -1) {
                        throw RuntimeException("Premature EOF")
                    }

                    val LOG_BYTES = 25
                    val reading = "Read $bytesRead bytes: " + String(
                        buffer,
                        offset,
                        min(LOG_BYTES.toDouble(), bytesRead.toDouble()).toInt()
                    ) + " ... " + String(
                        buffer,
                        max(0.0, (offset + bytesRead - LOG_BYTES).toDouble()).toInt(),
                        min(LOG_BYTES.toDouble(), bytesRead.toDouble()).toInt()
                    )
                    Log.d(
                        "NetworkBug", reading
                    )
                    logCallback?.invoke(reading)
                    offset += bytesRead
                }
            } catch (e: ConnectException) {
                // Handle ConnectException specifically (connection refused, timed out, etc.)
                val errorMessage = "Connection Error: ${e.message}"
                Log.e("NetworkBug", errorMessage, e)
                logCallback?.invoke(errorMessage)
                // You can decide how to proceed here:
                // - Retry the connection
                // - Stop the loop
                // - Inform the user
                // throw RuntimeException(e) // Or, re-throw if you want to crash here
                return // Exit the function in case of error.
            } catch (e: UnknownHostException) {
                // Handle UnknownHostException (DNS resolution failed)
                val errorMessage = "Unknown Host: ${e.message}"
                Log.e("NetworkBug", errorMessage, e)
                logCallback?.invoke(errorMessage)
                return // Exit the function in case of error.
            } catch (e: IOException) {
                // Handle other IOExceptions
                val errorMessage = "IO Error: ${e.message}"
                Log.e("NetworkBug", errorMessage, e)
                logCallback?.invoke(errorMessage)
                return // Exit the function in case of error.
                // throw RuntimeException(e) // Or, re-throw if you want to crash here
            } catch (e: RuntimeException) {
                // Handle RuntimeExceptions
                val errorMessage = "Runtime Error: ${e.message}"
                Log.e("NetworkBug", errorMessage, e)
                logCallback?.invoke(errorMessage)
                return // Exit the function in case of error.
            }
        }
        val endTime = System.nanoTime() // Capture end time
        val elapsedTimeMs = (endTime - startTime) / 1_000_000 // Calculate elapsed time in milliseconds
        onLoopDone?.invoke(elapsedTimeMs) // Pass elapsed time to callback
    }
}