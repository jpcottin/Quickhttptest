package com.example.quickhttptest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal const val MIN_BUFFER_SIZE = 1024
internal const val MAX_BUFFER_SIZE = 1048576
private const val DEFAULT_MAX_LOOPS = 3
private const val DEFAULT_BUFFER_SIZE = 8192

data class MainUiState(
    val loopValue: Int = 0,
    val logMessages: List<String> = emptyList(),
    val isLoopDone: Boolean = false,
    val isRunning: Boolean = false,
    val elapsedTime: Long = 0L,
    val selectedUrlType: String = "distant",
    val maxLoops: Int = DEFAULT_MAX_LOOPS,
    val loopInputText: String = DEFAULT_MAX_LOOPS.toString(),
    val showLoopError: Boolean = false,
    val bufferSize: Int = DEFAULT_BUFFER_SIZE,
    val bufferSizeInputText: String = DEFAULT_BUFFER_SIZE.toString(),
    val showBufferError: Boolean = false
)

class MainViewModel(
    private val httpTest: HttpTest = HttpTest()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    val distantUrl = "http://flexpansion.com/public/100.txt"
    val localUrl = "http://10.0.2.2:8000/100.txt"

    private var testJob: Job? = null

    fun onUrlTypeSelected(type: String) {
        _uiState.update { it.copy(selectedUrlType = type) }
    }

    fun onLoopInputChanged(text: String) {
        val num = text.toIntOrNull()
        val valid = num != null && num > 0
        _uiState.update {
            it.copy(
                loopInputText = text,
                maxLoops = if (valid) num!! else it.maxLoops,
                showLoopError = !valid
            )
        }
    }

    fun onBufferSizeInputChanged(text: String) {
        val num = text.toIntOrNull()
        val valid = num != null && num in MIN_BUFFER_SIZE..MAX_BUFFER_SIZE
        _uiState.update {
            it.copy(
                bufferSizeInputText = text,
                bufferSize = if (valid) num!! else it.bufferSize,
                showBufferError = !valid
            )
        }
    }

    fun toggleTest() {
        val state = _uiState.value
        if (state.isRunning) {
            testJob?.cancel()
            _uiState.update { it.copy(isRunning = false) }
            return
        }
        if (state.showLoopError || state.showBufferError) return

        val url = if (state.selectedUrlType == "distant") distantUrl else localUrl
        val maxLoops = state.maxLoops
        val bufferSize = state.bufferSize

        _uiState.update {
            it.copy(isRunning = true, isLoopDone = false, elapsedTime = 0L, logMessages = emptyList(), loopValue = 0)
        }

        testJob = viewModelScope.launch {
            httpTest.test(
                url = url,
                maxLoops = maxLoops,
                bufferSize = bufferSize,
                updateLoop = { newValue ->
                    _uiState.update { it.copy(loopValue = newValue) }
                },
                logCallback = { newMessage ->
                    _uiState.update { current ->
                        current.copy(logMessages = (listOf(newMessage) + current.logMessages).take(5))
                    }
                },
                onLoopDone = { time ->
                    _uiState.update { current ->
                        current.copy(isLoopDone = true, elapsedTime = time, loopValue = maxLoops, isRunning = false)
                    }
                }
            )
        }
    }
}
