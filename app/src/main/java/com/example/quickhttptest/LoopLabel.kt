package com.example.quickhttptest

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.quickhttptest.ui.theme.QuickhttptestTheme

@Composable
fun LoopLabel(modifier: Modifier = Modifier, loop: Int) {
    Text(
        text = "Loop: $loop",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun LoopLabelPreview() {
    QuickhttptestTheme {
        LoopLabel(loop = 42)
    }
}