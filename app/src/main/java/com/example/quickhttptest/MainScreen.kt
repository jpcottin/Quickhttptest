package com.example.quickhttptest

import android.text.method.ScrollingMovementMethod
import android.widget.TextView
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
import androidx.compose.ui.platform.testTag // Import testTag!


private const val DEFAULT_MAX_LOOPS = 3

@Composable
fun MainScreen() {
    var loopValue by remember { mutableIntStateOf(0) }
    var logMessages by remember { mutableStateOf(listOf<String>()) }
    var isLoopDone by remember { mutableStateOf(false) }
    var selectedUrlType by remember { mutableStateOf("distant") }
    var startTest by remember { mutableStateOf(false) }
    var maxLoops by remember { mutableIntStateOf(DEFAULT_MAX_LOOPS) }
    var loopInputText by remember { mutableStateOf(DEFAULT_MAX_LOOPS.toString()) }
    var showError by remember { mutableStateOf(false) }
    var elapsedTime by remember { mutableStateOf(0L) } // Store elapsed time


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
                isLoopDone = false
                startTest = it
                elapsedTime = 0 // Reset elapsed time
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
            showError = showError,
            distantUrl = distantUrl,
            localUrl = localUrl,
            elapsedTime = elapsedTime, // Pass to MainContent
            onLoopDone = { time ->  // Receive in MainScreen
                isLoopDone = true
                elapsedTime = time
            }
        )
    }
    if (startTest) {

        val thread = Thread {
            val url = if (selectedUrlType == "distant") distantUrl else localUrl
            HttpTest.test(
                url = url,
                maxLoops = maxLoops,
                updateLoop = { newValue ->  loopValue = newValue  },
                logCallback = { newMessage ->
                    logMessages = (listOf(newMessage) + logMessages).take(5)
                },
                onLoopDone = { time ->
                    isLoopDone = true // Update isLoopDone here
                    elapsedTime = time// Update elapsed time
                    loopValue = maxLoops
                }

            )
            startTest = false
        }
        thread.start()
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
    startTest: Boolean,  // Corrected type
    onStartTest: (Boolean) -> Unit, // Corrected type
    maxLoops: Int,
    loopInputText: String,
    onLoopInputChanged: (String) -> Unit,
    showError: Boolean,
    distantUrl: String,
    localUrl: String,
    elapsedTime: Long,
    onLoopDone : (Long) -> Unit
) {
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
                    onClick = { onUrlTypeSelected("distant") },
                    modifier = Modifier.testTag("radioButton_Distant URL")
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
                    onClick = { onUrlTypeSelected("local") },
                    modifier = Modifier.testTag("radioButton_Local URL")
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
            DoneLabel(elapsedTime = elapsedTime, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
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
                    setHorizontallyScrolling(false)
                    setTextAlignment(android.view.View.TEXT_ALIGNMENT_VIEW_START)
                }
            },
            update = { textView ->
                textView.text = logMessages.joinToString("\n")
            }
        )
    }
}

@Composable
fun DoneLabel(modifier: Modifier = Modifier, elapsedTime: Long) {
    Text(
        text = "DONE in $elapsedTime ms",
        fontSize = 24.sp,
        modifier = modifier.padding(16.dp)
    )
}

// ---  Previews ---
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
                showError = false,
                distantUrl = "http://example.com/distant",
                localUrl = "http://10.0.2.2:8000/local",
                elapsedTime = 0,
                onLoopDone = {}
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
                loopInputText = "",
                onLoopInputChanged = {},
                showError = true,
                distantUrl = "http://example.com/distant",
                localUrl = "http://10.0.2.2:8000/local",
                elapsedTime = 0,
                onLoopDone = {}
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
                isLoopDone = true,
                selectedUrlType = "distant",
                onUrlTypeSelected = {},
                startTest = false,
                onStartTest = {},
                maxLoops = 99,
                loopInputText = "99",
                onLoopInputChanged = {},
                showError = false,
                distantUrl = "http://example.com/distant",
                localUrl = "http://10.0.2.2:8000/local",
                elapsedTime = 1234,
                onLoopDone = {}
            )
        }
    }
}