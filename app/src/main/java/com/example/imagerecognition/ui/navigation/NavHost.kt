package com.example.imagerecognition.ui.navigation

import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import com.example.imagerecognition.ui.CameraPreview

@Composable
fun AppNavHost(navHostController: NavHostController = rememberNavController()) {
    val navGraph = remember(navHostController) {
        navHostController.createGraph(
            startDestination = CameraPreviewRoute
        ) {
            composable<CameraPreviewRoute> {
//                CameraPreview()
            }
        }
    }
    
    NavHost(navHostController, graph = navGraph)
}