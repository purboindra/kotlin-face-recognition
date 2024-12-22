package com.example.imagerecognition

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.imagerecognition.ui.CameraPreview
import com.example.imagerecognition.ui.navigation.AppNavHost
import com.example.imagerecognition.ui.theme.ImageRecognitionTheme
import com.example.imagerecognition.utils.CameraUtils
import com.example.imagerecognition.utils.takePhoto
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        if (!CameraUtils.hasRequiredPermission(this)) {
            ActivityCompat.requestPermissions(
                this, CameraUtils.CAMERAX_PERMISSIONS, 0
            )
        }
        
        setContent {
            ImageRecognitionTheme {
                
                val coroutineScope = rememberCoroutineScope()
                val scaffoldState = rememberBottomSheetScaffoldState()
                
                val controller = remember {
                    LifecycleCameraController(this@MainActivity).apply {
                        setEnabledUseCases(
                            CameraController.IMAGE_CAPTURE,
                        )
                    }
                }
                
                val viewModel = viewModel<MainViewModel>()
                val bitmaps by viewModel.bitmap.collectAsState()
                
                BottomSheetScaffold(
                    scaffoldState = scaffoldState,
                    sheetPeekHeight = 0.dp,
                    sheetContent = {
                        PhotoBottomSheetContent(
                            bitmaps = bitmaps,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                ) { padding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        CameraPreview(
                            modifier = Modifier.fillMaxSize(),
                            cameraController = controller
                        )
                       Row (modifier = Modifier.safeContentPadding()) {
                           IconButton(
                               onClick = {
                                   coroutineScope.launch {
                                       scaffoldState.bottomSheetState.expand()
                                   }
                               }
                           ) {
                               Icon(
                                   imageVector = Icons.Default.Photo,
                                   contentDescription = "Open gallery"
                               )
                           }
                           IconButton(
                               onClick = {
                                   controller.cameraSelector =
                                       if (controller.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                                           CameraSelector.DEFAULT_FRONT_CAMERA
                                       } else CameraSelector.DEFAULT_BACK_CAMERA
                               },
                           ) {
                               Icon(
                                   imageVector = Icons.Filled.Cameraswitch,
                                   contentDescription = "Switch Camera"
                               )
                           }
                           
                           IconButton(
                               onClick = {
                                   takePhoto(
                                       controller = controller,
                                       onPhotoTaken = viewModel::onTakePhoto,
                                       context = this@MainActivity
                                   )
                               }
                           ) {
                               Icon(
                                   imageVector = Icons.Filled.Camera,
                                   contentDescription = "Take photo"
                               )
                           }
                       }
                    }
                }
            }
        }
    }
}

@Composable
fun MainAppContent() {
    val navController = rememberNavController()
    AppNavHost(navController)
}