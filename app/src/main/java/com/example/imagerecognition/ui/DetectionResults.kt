package com.example.imagerecognition.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun DetectionResults(results: List<String>) {
    Column {
        Text(text = "Detection Results:", style = MaterialTheme.typography.bodyLarge)
        results.forEach { result ->
            Text(text = result, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
