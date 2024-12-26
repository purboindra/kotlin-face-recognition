package com.example.imagerecognition.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import androidx.navigation.navArgument
import com.example.imagerecognition.ui.viewmodel.MainViewModel
import com.example.imagerecognition.ui.screen.CameraPreview
import com.example.imagerecognition.ui.screen.CameraResultsScreen
import com.example.imagerecognition.ui.screen.RegisterScreen
import com.example.imagerecognition.ui.screen.ShowFaceDetectionResult

@Composable
fun AppNavHost(navHostController: NavHostController = rememberNavController()) {

    val mainViewModel: MainViewModel = hiltViewModel()

    val navGraph = remember(navHostController) {
        navHostController.createGraph(
            startDestination = Register
        ) {
            composable<CameraResults> {
                CameraResultsScreen(
                    mainViewModel = mainViewModel,
                    navHostController = navHostController
                )
            }

            composable<Register> {
                RegisterScreen(navHostController = navHostController)
            }

            composable(
                route = "camera_preview/{isLivePhoto}",
                arguments = listOf(
                    navArgument(
                        "isLivePhoto",
                    ) {
                        type = NavType.BoolType
                    }
                )
            ) { backStackEntry ->
                val isLivePhoto = backStackEntry.arguments?.getBoolean("isLivePhoto") ?: false
                println("isLivePhoto NavHost: ${isLivePhoto}")
                CameraPreview(
                    navHostController = navHostController,
                    isLivePhoto = isLivePhoto,
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