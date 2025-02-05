package com.example.quickhttptest

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.quickhttptest.ui.theme.QuickhttptestTheme
import java.io.IOException
import java.net.URL
import kotlin.math.max
import kotlin.math.min
import com.example.quickhttptest.ui.theme.Purple80

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            QuickhttptestTheme {
                var loopValue by remember { mutableIntStateOf(0) }
                var logMessages by remember { mutableStateOf(listOf<String>()) }
                var isLoopDone by remember { mutableStateOf(false) }
                var selectedUrlType by remember { mutableStateOf("distant") }
                var startTest by remember { mutableStateOf(false) }
                var maxLoops by remember { mutableIntStateOf(99) }
                var loopInputText by remember { mutableStateOf("99") }
                var showError by remember { mutableStateOf(false) }


                val distantUrl = "http://flexpansion.com/public/100.txt"
                val localUrl = "http://10.0.2.2:8000/100.txt"

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    ) {
                        Text(
                            text = "HTTP test",
                            fontSize = 22.sp,
                            color = Purple80,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            textAlign = TextAlign.Center
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = selectedUrlType == "distant",
                                    onClick = { selectedUrlType = "distant" }
                                )
                                Column {
                                    Text(text = "Distant URL", modifier = Modifier.padding(start = 8.dp))
                                    Text(
                                        text = distantUrl,
                                        modifier = Modifier.padding(start = 8.dp),
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = selectedUrlType == "local",
                                    onClick = { selectedUrlType = "local" }
                                )
                                Column {
                                    Text(text = "Local URL", modifier = Modifier.padding(start = 8.dp))
                                    Text(
                                        text = localUrl,
                                        modifier = Modifier.padding(start = 8.dp),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = loopInputText,
                            onValueChange = {
                                loopInputText = it
                                try {
                                    val num = it.toInt()
                                    if (num > 0) {
                                        maxLoops = num
                                        showError = false
                                    } else {
                                        showError = true
                                    }
                                } catch (e: NumberFormatException) {
                                    showError = true
                                }
                            },
                            label = { Text("Number of Loops") },
                            isError = showError,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                        if (showError) {
                            Text("Please enter a positive number", color = Color.Red, modifier = Modifier.padding(start = 16.dp))
                        }

                        Button(
                            onClick = {
                                if (!showError) {
                                    startTest = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(text = "Start Test")
                        }
                        LoopLabel(
                            modifier = Modifier.padding(16.dp),
                            loop = loopValue
                        )
                        LogDisplay(logMessages, Modifier.weight(1f))
                        if (isLoopDone) {
                            DoneLabel(modifier = Modifier.align(Alignment.CenterHorizontally))
                        }
                    }
                }
                if (startTest) {
                    val thread = Thread {
                        val url = if (selectedUrlType == "distant") distantUrl else localUrl
                        MainActivity.test(
                            url = url,
                            maxLoops = maxLoops,
                            updateLoop = { newValue -> runOnUiThread { loopValue = newValue } },
                            logCallback = { newMessage -> runOnUiThread { logMessages = (listOf(newMessage) + logMessages).take(5) } },
                            onLoopDone = { runOnUiThread { isLoopDone = true } }
                        )
                        runOnUiThread { startTest = false }
                    }
                    thread.start()
                }
            }
        }
    }

    companion object {
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

                    var contentLength = conn.getContentLength()
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
}

@Composable
fun LoopLabel( modifier: Modifier = Modifier, loop: Int) {
    Text(
        text = "Loop: $loop",
        modifier = modifier
    )
}

@Composable
fun LogDisplay(logMessages: List<String>, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surfaceVariant) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                TextView(context).apply {
                    movementMethod = ScrollingMovementMethod()
                    // Enable text wrapping
                    setHorizontallyScrolling(false)
                    setTextAlignment(android.view.View.TEXT_ALIGNMENT_VIEW_START)
                }
            },
            update = { textView ->
                // Join log messages with newlines
                textView.text = logMessages.joinToString("\n")
            }
        )
    }
}

@Composable
fun DoneLabel(modifier: Modifier = Modifier) {
    Text(
        text = "DONE",
        fontSize = 48.sp,
        modifier = modifier.padding(16.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun LoopLabelPreview() {
    QuickhttptestTheme {
        LoopLabel( loop = 42)
    }
}