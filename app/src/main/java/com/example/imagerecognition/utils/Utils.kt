package com.example.imagerecognition.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.Image
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale


fun takePhoto(
    controller: LifecycleCameraController,
    onPhotoTaken: (Bitmap) -> Unit,
    context: Context
) {
    controller.takePicture(
        ContextCompat.getMainExecutor(context),
        object : OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                super.onCaptureSuccess(image)
                
                val matrix = Matrix().apply {
                    postRotate(image.imageInfo.rotationDegrees.toFloat())
                }
                
                val rotatedBitmap = Bitmap.createBitmap(
                    image.toBitmap(),
                    0, 0, image.width, image.height, matrix, true
                )
                
                Log.d("Camera", "Photo taken: $rotatedBitmap")
                
                val inputImage = image.toInputImage()
                
                // ML Kit Object Detection
                val objectDetector = ObjectDetection.getClient(mlKitObjectDetection)
                objectDetector.process(inputImage)
                    .addOnSuccessListener { detectedObjects ->
                        val recognizedLabels = detectedObjects.flatMap { obj ->
                            obj.labels.map { it.text }
                        }
                        
                        Log.d("Camera", "Recognized labels: $recognizedLabels")
                        
//                        onRecognitionComplete(recognizedLabels)
                    }
                    .addOnFailureListener { e ->
                        Log.e("Camera", "Recognition failed: ${e.message}")
//                        onRecognitionComplete(emptyList()) // Return empty list on failure
                    }
                    .addOnCompleteListener {
                        image.close() // Always close the ImageProxy
                    }
                
                
                onPhotoTaken(rotatedBitmap)
            }
            
            override fun onError(exception: ImageCaptureException) {
                super.onError(exception)
                Log.e("Camera", "Couldn't take photo: ", exception)
            }
        }
    )
}

object CameraUtils {
    val CAMERAX_PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
    )
    
    fun hasRequiredPermission(context: Context): Boolean {
        return CAMERAX_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                context,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}


@OptIn(ExperimentalGetImage::class)
fun ImageProxy.toInputImage(): InputImage {
    val rotation = imageInfo.rotationDegrees
    val mediaImage = image ?: IllegalArgumentException("Image is null")
    return InputImage.fromMediaImage(mediaImage as Image, rotation)
}

val mlKitObjectDetection =
    ObjectDetectorOptions.Builder().setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
        .enableClassification().build()
val objectDetector = ObjectDetection.getClient(mlKitObjectDetection)