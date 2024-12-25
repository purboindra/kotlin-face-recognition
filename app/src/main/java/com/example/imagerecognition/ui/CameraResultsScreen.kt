package com.example.imagerecognition.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.imagerecognition.MainViewModel
import com.example.imagerecognition.ui.navigation.CameraPreview
import com.example.imagerecognition.utils.serializeBitmap
import com.example.imagerecognition.utils.serializeFaces
import com.example.imagerecognition.utils.takePhoto
import kotlinx.coroutines.launch

@Composable
fun CameraResultsScreen(mainViewModel: MainViewModel, navHostController: NavHostController) {
    val context = LocalContext.current
    
    val coroutineScope = rememberCoroutineScope()
    
    val registeredPhoto by mainViewModel.registeredPhotoBitmap.collectAsState()
    val livePhoto by mainViewModel.livePhotoBitmap.collectAsState()
    
    val isLoadingIdentify by mainViewModel.isLoadingIdentify.collectAsState()
    
    val registeredFaceNotDetected by mainViewModel.registeredFaceNotDetected.collectAsState()
    val liveFaceNotDetected by mainViewModel.liveFaceNotDetected.collectAsState()
    
    val registeredFaces by mainViewModel.registeredFaces.collectAsState()
    
    LaunchedEffect(Unit) {
        mainViewModel.onLoadImageBitmapFromFile(context)
    }
    
    Scaffold(
        modifier = Modifier
            .safeContentPadding()
            .padding(vertical = 48.dp, horizontal = 12.dp)
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    if (registeredPhoto != null) Image(
                        bitmap = registeredPhoto!!.asImageBitmap(),
                        contentDescription = "Image Preview",
                        modifier = Modifier
                            .width(102.dp)
                            .height(102.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                    ) else ElevatedButton(
                        onClick = {
                            mainViewModel.setIsLivePhoto(false)
                            navHostController.navigate(CameraPreview)
                        }
                    ) {
                        Text("Register Photo")
                    }
                    Box(modifier = Modifier.height(10.dp))
                    if (registeredFaceNotDetected != null) {
                        Text(
                            registeredFaceNotDetected!!,
                            color = Color.Red,
                        )
                    }
                }
                
                Box(modifier = Modifier.width(10.dp))
                
                
                Column(horizontalAlignment = Alignment.End) {
                    if (livePhoto != null) Image(
                        bitmap = livePhoto!!.asImageBitmap(),
                        contentDescription = "Image Preview",
                        modifier = Modifier
                            .width(102.dp)
                            .height(102.dp)
                            .clip(CircleShape)
                            .clickable {
                                mainViewModel.setIsLivePhoto(true)
                                navHostController.navigate(CameraPreview)
                            },
                        contentScale = ContentScale.Crop,
                    ) else ElevatedButton(
                        onClick = {
                            mainViewModel.setIsLivePhoto(true)
                            navHostController.navigate(CameraPreview)
                        },
                        enabled = registeredPhoto != null
                    ) {
                        Text("Take Photo")
                    }
                    Box(modifier = Modifier.height(10.dp))
                    if (liveFaceNotDetected != null) {
                        Text(
                            liveFaceNotDetected!!,
                            color = Color.Red,
                        )
                    }
                }
                
            }
            
            Box(modifier = Modifier.height(10.dp))
            ElevatedButton(
                onClick = {
                    mainViewModel.onCompareFaces(registeredPhoto, livePhoto)
                },
                enabled = livePhoto != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoadingIdentify) CircularProgressIndicator() else Text("Identify")
            }
            Box(modifier = Modifier.height(10.dp))
//            ElevatedButton(
//                onClick = {
//                    if (registeredPhoto != null) {
//                        navHostController.navigate(
//                            "show_detection_result",
//                        )
//                    }
//                },
//                enabled = registeredFaces.isNotEmpty(),
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Text("Show Detection Results")
//            }
        }
    }
}