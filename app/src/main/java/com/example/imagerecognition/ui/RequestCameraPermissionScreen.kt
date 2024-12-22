//package com.example.imagerecognition.ui
//
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//
//@Composable
//fun RequestCameraPermission(onGranted: () -> Unit) {
//    val permissionState = rememberRequestPermissionState(android.Manifest.permission.CAMERA)
//
//    LaunchedEffect(permissionState.status) {
//        if (permissionState.status.isGranted) {
//            onGranted()
//        } else {
//            permissionState.launchPermissionRequest()
//        }
//    }
//}
