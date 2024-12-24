package com.example.imagerecognition.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.Image
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.File
import java.io.FileOutputStream
import kotlin.math.sqrt


fun takePhoto(
    controller: LifecycleCameraController,
    onPhotoTaken: (Bitmap) -> Unit,
    context: Context,
    isLivePhoto: Boolean,
) {
    controller.takePicture(
        ContextCompat.getMainExecutor(context),
        object : OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                super.onCaptureSuccess(image)

                try {
                    val matrix = Matrix().apply {
                        postRotate(image.imageInfo.rotationDegrees.toFloat())
                    }

                    val rotatedBitmap = Bitmap.createBitmap(
                        image.toBitmap(),
                        0, 0, image.width, image.height, matrix, true
                    )

                    Log.d("CameraCapture", "Photo taken successfully: $rotatedBitmap")
                    Log.d("CameraCapture", "Is live photo: $isLivePhoto")

                    if (!isLivePhoto) {
                        // Save locally
                        saveBitmapToFile(rotatedBitmap, "registered_face.png", context)
                    }

                    onPhotoTaken(rotatedBitmap)
                } catch (e: Exception) {
                    Log.e("CameraCapture", "Error processing image: ${e.message}", e)
                } finally {
                    image.close()
                }
            }

            override fun onError(exception: ImageCaptureException) {
                super.onError(exception)
                Log.e("Camera onError", "Couldn't take photo: ", exception)
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

fun detectFaceFeatures(
    bitmap: Bitmap, onFeaturesExtracted: (List<Face>) -> Unit,
    onError: (Exception) -> Unit
) {
    val inputImage = InputImage.fromBitmap(bitmap, 0)
    val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE).enableTracking()
            .build()
    )
    detector.process(inputImage).addOnSuccessListener { faces ->
        if (faces.isNotEmpty()) {
            onFeaturesExtracted(faces.toList())
        } else {
            onError(Exception("No faces detected"))
        }
    }.addOnFailureListener { e ->
        onError(e)
    }
}

fun compareFaces(registeredFace: Face, liveFace: Face): Boolean {
    val distanceThreshold = 50
    val dx = registeredFace.boundingBox.centerX() - liveFace.boundingBox.centerX()
    val dy = registeredFace.boundingBox.centerY() - liveFace.boundingBox.centerY()
    val distance = sqrt((dx * dy + dy * dx).toDouble())
    return distance < distanceThreshold
}

fun cosineSimilarity(embedding1: FloatArray, embedding2: FloatArray): Float {
    val dotProduct = embedding1.zip(embedding2) { a, b -> a * b }.sum()
    val norm1 = Math.sqrt(embedding1.map { it * it }.sum().toDouble())
    val norm2 = Math.sqrt(embedding2.map { it * it }.sum().toDouble())
    return (dotProduct / (norm1 * norm2)).toFloat()
}


fun saveBitmapToFile(bitmap: Bitmap, fileName: String, context: Context) {
    val file = File(context.filesDir, fileName)
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }
}

fun loadBitmapFromFile(fileName: String, context: Context): Bitmap? {
    val file = File(context.filesDir, fileName)
    Log.d("loadBitmapFromField", "File: ${fileName} exists: ${file.exists()} ${file.absolutePath}")
    return if (file.exists()) {
        BitmapFactory.decodeFile(file.absolutePath)
    } else {
        null
    }
}