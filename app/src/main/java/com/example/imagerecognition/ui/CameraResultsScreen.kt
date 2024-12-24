package com.example.imagerecognition.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.imagerecognition.MainViewModel
import com.example.imagerecognition.ui.navigation.CameraPreview
import com.example.imagerecognition.utils.takePhoto
import kotlinx.coroutines.launch

@Composable
fun CameraResultsScreen(mainViewModel: MainViewModel, navHostController: NavHostController) {
    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()

    val bitmaps by mainViewModel.bitmap.collectAsState()

    val isLoadingIdentify by mainViewModel.isLoadingIdentify.collectAsState()

    LaunchedEffect(Unit) {
        mainViewModel.onLoadImageBitmapFromFile(context)
    }

    Scaffold(modifier = Modifier.safeContentPadding()) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (bitmaps.isNotEmpty()) Image(
                    bitmap = bitmaps.first().asImageBitmap(),
                    contentDescription = "Image Preview",
                    modifier = Modifier
                        .width(42.dp)
                        .height(42.dp)
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

                if (bitmaps.size > 1) Image(
                    bitmap = bitmaps[1].asImageBitmap(),
                    contentDescription = "Image Preview",
                    modifier = Modifier
                        .width(42.dp)
                        .height(42.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                ) else ElevatedButton(
                    onClick = {
                        mainViewModel.setIsLivePhoto(true)
                        navHostController.navigate(CameraPreview)
                    },
                    enabled = bitmaps.isNotEmpty()
                ) {
                    Text("Take Photo")
                }
            }

            Box(modifier = Modifier.height(10.dp))
            ElevatedButton(
                onClick = {
                    mainViewModel.onCompareFaces(bitmap = bitmaps.first())
                },
                enabled = bitmaps.size > 1,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoadingIdentify) CircularProgressIndicator() else Text("Identify")
            }
        }
    }
}