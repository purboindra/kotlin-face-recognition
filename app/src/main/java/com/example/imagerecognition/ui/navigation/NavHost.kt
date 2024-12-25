package com.example.imagerecognition.ui.navigation

import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import androidx.navigation.navArgument
import com.example.imagerecognition.MainViewModel
import com.example.imagerecognition.ui.CameraPreview
import com.example.imagerecognition.ui.CameraResultsScreen
import com.example.imagerecognition.ui.ShowFaceDetectionResult
import com.example.imagerecognition.utils.deserializableFaceData
import com.example.imagerecognition.utils.deserializeBitmap

@Composable
fun AppNavHost(navHostController: NavHostController = rememberNavController()) {
    
    val mainViewModel: MainViewModel = hiltViewModel()
    
    val navGraph = remember(navHostController) {
        navHostController.createGraph(
            startDestination = CameraResults
        ) {
            composable<CameraResults> {
                CameraResultsScreen(
                    mainViewModel = mainViewModel,
                    navHostController = navHostController
                )
            }
            
            composable<CameraPreview> {
                CameraPreview(
                    navHostController = navHostController,
                    mainViewModel = mainViewModel,
                    isLivePhoto = mainViewModel.isLivePhoto.value
                )
            }
            
            composable(
                route = "show_detection_result",
//                arguments = listOf(
//                    navArgument("bitmap") {
//                        type = NavType.StringType
//                    },
//                    navArgument("faces") {
//                        type = NavType.StringType
//                    },
//                )
            ) { backStackEntry ->
                ShowFaceDetectionResult(mainViewModel)

//                val bitmapJson = backStackEntry.arguments?.getString("bitmap")
//                val dataJson = backStackEntry.arguments?.getString("faces")
//                val bitmap = bitmapJson?.let {
//                    deserializeBitmap(it)
//                }
//                val faces = dataJson?.let {
//                    deserializableFaceData(it)
//                }
//
//                if (bitmap != null && faces != null) {
//                    ShowFaceDetectionResult(mainViewModel)
//                }
                
            }
        }
    }
    
    NavHost(navHostController, graph = navGraph)
}