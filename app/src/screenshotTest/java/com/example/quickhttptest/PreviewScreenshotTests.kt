package com.example.quickhttptest

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest

// Wraps the existing main-source previews (self-contained sample state) so the
// screenshot plugin, which only scans this source set, picks them up.

@PreviewTest
@Preview(name = "Initial", showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun InitialStateScreenshot() = DefaultPreview()

@PreviewTest
@Preview(name = "Error", showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun ErrorStateScreenshot() = ErrorPreview()

@PreviewTest
@Preview(name = "Running", showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun RunningStateScreenshot() = RunningPreview()

@PreviewTest
@Preview(name = "Done", showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun DoneStateScreenshot() = DonePreview()

@PreviewTest
@Preview(name = "LoopLabel", showBackground = true)
@Composable
fun LoopLabelScreenshot() = LoopLabelPreview()
