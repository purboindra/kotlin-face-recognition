package com.example.imagerecognition.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.imagerecognition.ui.viewmodel.MainViewModel

@Composable
fun ShowFaceDetectionResult(
    mainViewModel: MainViewModel,
) {
    
    val registeredFace by mainViewModel.registeredPhotoBitmap.collectAsState()
    val registeredFaces by mainViewModel.registeredFaces.collectAsState()
    
    Box(modifier = Modifier.fillMaxSize()) {
        if (registeredFace != null) Image(
            bitmap = registeredFace!!.asImageBitmap(),
            contentDescription = "Detected Face",
            modifier = Modifier.fillMaxSize()
        )
        
        // Overlay bounding boxes
        Canvas(modifier = Modifier.fillMaxSize()) {
            registeredFaces.forEach { face ->
                // Draw bounding box
                val boundingBox = face.boundingBox
                drawRect(
                    color = Color.Red,
                    topLeft = Offset(0.3f, 0.2f),
                    size = Size(
                        0.3f, 0.5f
                    ),
                    style = Stroke(width = 4f)
                )
            }
        }
    }
}
