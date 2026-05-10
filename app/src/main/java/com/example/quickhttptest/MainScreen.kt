package com.example.quickhttptest

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.quickhttptest.ui.theme.QuickhttptestTheme

@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MainContent(
        innerPadding = WindowInsets.safeContent.asPaddingValues(),
        loopValue = uiState.loopValue,
        logMessages = uiState.logMessages,
        isLoopDone = uiState.isLoopDone,
        selectedUrlType = uiState.selectedUrlType,
        onUrlTypeSelected = viewModel::onUrlTypeSelected,
        isRunning = uiState.isRunning,
        onToggleTest = viewModel::toggleTest,
        loopInputText = uiState.loopInputText,
        onLoopInputChanged = viewModel::onLoopInputChanged,
        showLoopError = uiState.showLoopError,
        bufferSizeInputText = uiState.bufferSizeInputText,
        onBufferSizeInputChanged = viewModel::onBufferSizeInputChanged,
        showBufferError = uiState.showBufferError,
        distantUrl = viewModel.distantUrl,
        localUrl = viewModel.localUrl,
        elapsedTime = uiState.elapsedTime
    )
}

@Composable
fun MainContent(
    innerPadding: PaddingValues,
    loopValue: Int,
    logMessages: List<String>,
    isLoopDone: Boolean,
    selectedUrlType: String,
    onUrlTypeSelected: (String) -> Unit,
    isRunning: Boolean,
    onToggleTest: () -> Unit,
    loopInputText: String,
    onLoopInputChanged: (String) -> Unit,
    showLoopError: Boolean,
    bufferSizeInputText: String,
    onBufferSizeInputChanged: (String) -> Unit,
    showBufferError: Boolean,
    distantUrl: String,
    localUrl: String,
    elapsedTime: Long
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(innerPadding)
    ) {
        Text(
            text = "HTTP test",
            fontSize = 22.sp,
            color = MaterialTheme.colorScheme.primary,
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
                    Text(text = distantUrl, modifier = Modifier.padding(start = 8.dp), fontSize = 10.sp)
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
                    Text(text = localUrl, modifier = Modifier.padding(start = 8.dp), fontSize = 10.sp)
                }
            }
        }
        OutlinedTextField(
            value = loopInputText,
            onValueChange = { onLoopInputChanged(it) },
            label = { Text("Number of Loops") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            singleLine = true,
            isError = showLoopError,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
        )
        if (showLoopError) {
            Text("Please enter a positive number", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(start = 16.dp))
        }
        OutlinedTextField(
            value = bufferSizeInputText,
            onValueChange = { onBufferSizeInputChanged(it) },
            label = { Text("Buffer Size (bytes)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            singleLine = true,
            isError = showBufferError,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
        )
        if (showBufferError) {
            Text(
                "Please enter a size between $MIN_BUFFER_SIZE and $MAX_BUFFER_SIZE",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
        Button(
            onClick = onToggleTest,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isRunning) Color.Red else MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Text(text = if (isRunning) "Stop Test" else "Start Test")
        }
        LoopLabel(modifier = Modifier.padding(horizontal = 16.dp), loop = loopValue)
        LogDisplay(logMessages, Modifier.fillMaxWidth())
        if (isLoopDone) {
            DoneLabel(elapsedTime = elapsedTime, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun LogDisplay(logMessages: List<String>, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = MaterialTheme.colorScheme.surfaceVariant) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)) {
            logMessages.forEach { message ->
                Text(text = message, fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp))
            }
        }
    }
}

@Composable
fun DoneLabel(modifier: Modifier = Modifier, elapsedTime: Long) {
    Text(
        text = "DONE in $elapsedTime ms",
        fontSize = 24.sp,
        textAlign = TextAlign.Center,
        modifier = modifier.padding(16.dp)
    )
}

// --- Previews ---
@Preview(showBackground = true, name = "Initial State", device = Devices.PIXEL_4)
@Composable
fun DefaultPreview() {
    QuickhttptestTheme {
        MainContent(
            innerPadding = PaddingValues(0.dp),
            loopValue = 0,
            logMessages = listOf("Log 1", "Log 2"),
            isLoopDone = false,
            selectedUrlType = "distant",
            onUrlTypeSelected = {},
            isRunning = false,
            onToggleTest = {},
            loopInputText = "3",
            onLoopInputChanged = {},
            showLoopError = false,
            bufferSizeInputText = "8192",
            onBufferSizeInputChanged = {},
            showBufferError = false,
            distantUrl = "http://example.com/distant",
            localUrl = "http://10.0.2.2:8000/local",
            elapsedTime = 0
        )
    }
}

@Preview(showBackground = true, name = "Error State", device = Devices.PIXEL_4)
@Composable
fun ErrorPreview() {
    QuickhttptestTheme {
        MainContent(
            innerPadding = PaddingValues(0.dp),
            loopValue = 5,
            logMessages = listOf("Log 1", "Log 2", "Log 3"),
            isLoopDone = false,
            selectedUrlType = "local",
            onUrlTypeSelected = {},
            isRunning = false,
            onToggleTest = {},
            loopInputText = "",
            onLoopInputChanged = {},
            showLoopError = true,
            bufferSizeInputText = "999",
            onBufferSizeInputChanged = {},
            showBufferError = true,
            distantUrl = "http://example.com/distant",
            localUrl = "http://10.0.2.2:8000/local",
            elapsedTime = 0
        )
    }
}

@Preview(showBackground = true, name = "Running State", device = Devices.PIXEL_4)
@Composable
fun RunningPreview() {
    QuickhttptestTheme {
        MainContent(
            innerPadding = PaddingValues(0.dp),
            loopValue = 42,
            logMessages = listOf("Log 1", "Log 2", "Log 3"),
            isLoopDone = false,
            selectedUrlType = "distant",
            onUrlTypeSelected = {},
            isRunning = true,
            onToggleTest = {},
            loopInputText = "100",
            onLoopInputChanged = {},
            showLoopError = false,
            bufferSizeInputText = "8192",
            onBufferSizeInputChanged = {},
            showBufferError = false,
            distantUrl = "http://example.com/distant",
            localUrl = "http://10.0.2.2:8000/local",
            elapsedTime = 0
        )
    }
}

@Preview(showBackground = true, name = "Done State", device = Devices.PIXEL_4)
@Composable
fun DonePreview() {
    QuickhttptestTheme {
        MainContent(
            innerPadding = PaddingValues(0.dp),
            loopValue = 100,
            logMessages = listOf("Log A", "Log B", "Log C"),
            isLoopDone = true,
            selectedUrlType = "distant",
            onUrlTypeSelected = {},
            isRunning = false,
            onToggleTest = {},
            loopInputText = "100",
            onLoopInputChanged = {},
            showLoopError = false,
            bufferSizeInputText = "8192",
            onBufferSizeInputChanged = {},
            showBufferError = false,
            distantUrl = "http://example.com/distant",
            localUrl = "http://10.0.2.2:8000/local",
            elapsedTime = 1234
        )
    }
}
