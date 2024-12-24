package com.example.imagerecognition.ui

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import com.example.imagerecognition.MainViewModel
import com.example.imagerecognition.ui.navigation.CameraResults
import com.example.imagerecognition.utils.takePhoto


@Composable
fun CameraPreview(
    mainViewModel: MainViewModel,
    navHostController: NavHostController,
    isLivePhoto:Boolean,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    val loadingImage by mainViewModel.loadingImage.collectAsState()
    val bitmap by mainViewModel.bitmap.collectAsState()

    val cameraController = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(
                CameraController.IMAGE_CAPTURE,
            )
        }
    }

    DisposableEffect(cameraController) {
        cameraController.bindToLifecycle(lifecycleOwner)
        Log.d("CameraLifecycle", "Camera bound to lifecycle")
        onDispose {
            Log.d("CameraLifecycle", "Camera unbound from lifecycle")
            cameraController.unbind()
        }
    }


    Box(modifier = Modifier.fillMaxSize()) {

        AndroidView(
            factory = {
                PreviewView(it).apply {
                    controller = cameraController
                    cameraController.bindToLifecycle(lifecycleOwner)
                }
            }, modifier = Modifier.fillMaxSize()
        )

        Button(
            onClick = {
                mainViewModel.changeLoading(true)
                takePhoto(context = context,
                    controller = cameraController,
                    isLivePhoto = isLivePhoto,
                    onPhotoTaken = { photo ->
                        mainViewModel.onTakePhoto(photo)
                        mainViewModel.changeLoading(false)
                        navHostController.popBackStack()
                    })

            }, modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            if (loadingImage) CircularProgressIndicator() else Text("Capture Photo")
        }
    }
}