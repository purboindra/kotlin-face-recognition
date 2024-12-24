package com.example.imagerecognition.ui.navigation

import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import com.example.imagerecognition.MainViewModel
import com.example.imagerecognition.ui.CameraPreview
import com.example.imagerecognition.ui.CameraResultsScreen

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
        }
    }

    NavHost(navHostController, graph = navGraph)
}