package com.example.quickhttptest

import android.util.Log
import java.io.IOException
import java.net.URL
import kotlin.math.max
import kotlin.math.min

object HttpTest {
    fun test(
        url: String,
        maxLoops: Int,
        updateLoop: (Int) -> Unit,
        logCallback: (String) -> Unit,
        onLoopDone: () -> Unit
    ) {
        for (i in 0..maxLoops) {
            val message = "Loop $i"
            Log.d("NetworkBug", message)
            updateLoop(i)
            logCallback(message)
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
                    logCallback(requesting)
                    val bytesRead = `is`.read(buffer, offset, bytesLeft)
                    if (bytesRead == -1) {
                        throw RuntimeException("Premature EOF")
                    }

                    val LOG_BYTES = 25
                    val reading = "Read $bytesRead bytes: " + kotlin.text.String(
                        buffer,
                        offset,
                        min(LOG_BYTES.toDouble(), bytesRead.toDouble()).toInt()
                    ) + " ... " + kotlin.text.String(
                        buffer,
                        max(0.0, (offset + bytesRead - LOG_BYTES).toDouble()).toInt(),
                        min(LOG_BYTES.toDouble(), bytesRead.toDouble()).toInt()
                    )
                    Log.d(
                        "NetworkBug", reading
                    )
                    logCallback(reading)
                    offset += bytesRead
                }
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }
        onLoopDone()
    }
}