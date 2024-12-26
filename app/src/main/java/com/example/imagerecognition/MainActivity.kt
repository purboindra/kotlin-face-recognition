package com.example.imagerecognition

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.core.app.ActivityCompat
import androidx.navigation.compose.rememberNavController
import com.example.imagerecognition.ui.navigation.AppNavHost
import com.example.imagerecognition.ui.theme.ImageRecognitionTheme
import com.example.imagerecognition.utils.CameraUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
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
                MainAppContent()
            }
        }
    }
}

@Composable
fun MainAppContent() {
    val navController = rememberNavController()
    AppNavHost(navController)
}