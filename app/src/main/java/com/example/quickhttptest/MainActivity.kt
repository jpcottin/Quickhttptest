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
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.quickhttptest.ui.theme.QuickhttptestTheme
import com.example.quickhttptest.ui.theme.Purple80
import java.io.IOException
import java.net.URL
import kotlin.math.max
import kotlin.math.min

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
                    MainContent(
                        innerPadding = innerPadding,
                        loopValue = loopValue,
                        logMessages = logMessages,
                        isLoopDone = isLoopDone,
                        selectedUrlType = selectedUrlType,
                        onUrlTypeSelected = { selectedUrlType = it },
                        startTest = startTest,
                        onStartTest = {
                            // Reset isLoopDone to false when starting a new test
                            isLoopDone = false
                            startTest = it
                        },
                        maxLoops = maxLoops,
                        loopInputText = loopInputText,
                        onLoopInputChanged = { text ->
                            loopInputText = text
                            try {
                                val num = text.toInt()
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
                        showError = showError
                    )
                }
                if (startTest) {
                    val thread = Thread {
                        val url = if (selectedUrlType == "distant") distantUrl else localUrl
                        MainActivity.test(
                            url = url,
                            maxLoops = maxLoops,
                            updateLoop = { newValue -> runOnUiThread { loopValue = newValue } },
                            logCallback = { newMessage ->
                                runOnUiThread {
                                    logMessages = (listOf(newMessage) + logMessages).take(5)
                                }
                            },
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
}

@Composable
fun MainContent(
    innerPadding: androidx.compose.foundation.layout.PaddingValues,
    loopValue: Int,
    logMessages: List<String>,
    isLoopDone: Boolean,
    selectedUrlType: String,
    onUrlTypeSelected: (String) -> Unit,
    startTest: Boolean,
    onStartTest: (Boolean) -> Unit,
    maxLoops: Int,  //Not used in the preview, but kept for consistency with the real app
    loopInputText: String,
    onLoopInputChanged: (String) -> Unit,
    showError: Boolean
) {
    Column(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
    ) {
        Text(
            text = "HTTP test",
            fontSize = 22.sp,
            color = Purple80, // Consider using MaterialTheme.colorScheme.primary
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
                    onClick = { onUrlTypeSelected("distant") }
                )
                Column {
                    Text(text = "Distant URL", modifier = Modifier.padding(start = 8.dp))
                    Text(
                        text = "http://flexpansion.com/public/100.txt", //Hardcoded for preview
                        modifier = Modifier.padding(start = 8.dp),
                        fontSize = 10.sp // Consider using MaterialTheme.typography
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selectedUrlType == "local",
                    onClick = { onUrlTypeSelected("local") }
                )
                Column {
                    Text(text = "Local URL", modifier = Modifier.padding(start = 8.dp))
                    Text(
                        text = "http://10.0.2.2:8000/100.txt", //Hardcoded for preview
                        modifier = Modifier.padding(start = 8.dp),
                        fontSize = 10.sp // Consider using MaterialTheme.typography
                    )
                }
            }
        }

        OutlinedTextField(
            value = loopInputText,
            onValueChange = { onLoopInputChanged(it) },
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
                    onStartTest(true)
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



@Composable
fun LoopLabel(modifier: Modifier = Modifier, loop: Int) {
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

@Preview(showBackground = true, name = "Initial State", device = Devices.PIXEL_4)
@Composable
fun DefaultPreview() {
    QuickhttptestTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            MainContent(
                innerPadding = innerPadding,
                loopValue = 0,
                logMessages = listOf("Log 1", "Log 2"),
                isLoopDone = false,
                selectedUrlType = "distant",
                onUrlTypeSelected = {},
                startTest = false,
                onStartTest = {},
                maxLoops = 99,
                loopInputText = "99",
                onLoopInputChanged = {},
                showError = false
            )
        }
    }
}

@Preview(showBackground = true, name = "Error State", device = Devices.PIXEL_4)
@Composable
fun ErrorPreview() {
    QuickhttptestTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            MainContent(
                innerPadding = innerPadding,
                loopValue = 5,
                logMessages = listOf("Log 1", "Log 2", "Log 3"),
                isLoopDone = false,
                selectedUrlType = "local",
                onUrlTypeSelected = {},
                startTest = false,
                onStartTest = {},
                maxLoops = 99,
                loopInputText = "", // Empty to simulate error
                onLoopInputChanged = {},
                showError = true // Show the error message
            )
        }
    }
}

@Preview(showBackground = true, name = "Done State", device = Devices.PIXEL_4)
@Composable
fun DonePreview() {
    QuickhttptestTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            MainContent(
                innerPadding = innerPadding,
                loopValue = 100,
                logMessages = listOf("Log A", "Log B", "Log C"),
                isLoopDone = true, // Show the "DONE" label
                selectedUrlType = "distant",
                onUrlTypeSelected = {},
                startTest = false,
                onStartTest = {},
                maxLoops = 99,
                loopInputText = "99",
                onLoopInputChanged = {},
                showError = false
            )
        }
    }
}