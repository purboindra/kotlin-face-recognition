package com.example.imagerecognition.ui.screen

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import com.example.imagerecognition.ui.viewmodel.MainViewModel
import com.example.imagerecognition.utils.takePhoto
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream


@Composable
fun CameraPreview(
    navHostController: NavHostController,
    isLivePhoto: Boolean,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    var loadingImage by remember {
        mutableStateOf(false)
    }

    var isUsingBackCamera by remember {
        mutableStateOf(true)
    }

    var image by remember {
        mutableStateOf<String?>(null)
    }

    val cameraController = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(
                CameraController.IMAGE_CAPTURE,
            )
        }
    }

    fun onTakePhoto(bitmap: Bitmap) {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        val encodedBitmap = Base64.encodeToString(byteArray, Base64.DEFAULT)
        image = encodedBitmap
    }


    fun toggleCamera() {
        isUsingBackCamera = !isUsingBackCamera
        cameraController.cameraSelector = if (isUsingBackCamera) {
            CameraSelector.DEFAULT_BACK_CAMERA
        } else {
            CameraSelector.DEFAULT_FRONT_CAMERA
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

        IconButton(
            onClick = {
                toggleCamera()
            },
            modifier = Modifier
                .padding(24.dp)
                .safeContentPadding()
        ) {
            Icon(
                Icons.Filled.Cameraswitch,
                contentDescription = "Switch Camera",
                tint = Color.White,
            )
        }

        Button(
            onClick = {
                loadingImage = true
                takePhoto(context = context,
                    controller = cameraController,
                    isLivePhoto = isLivePhoto,
                    onPhotoTaken = { photo ->
                        onTakePhoto(photo)
                        loadingImage = false
                        navHostController.previousBackStackEntry?.savedStateHandle?.set(
                            "image",
                            image
                        )
                        navHostController.popBackStack()
                    })

            }, modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp)
                .safeContentPadding()
        ) {
            if (loadingImage) CircularProgressIndicator() else Text("Capture Photo")
        }
    }
}