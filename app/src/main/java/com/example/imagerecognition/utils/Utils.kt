package com.example.imagerecognition.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.Image
import android.util.Base64
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.core.content.ContextCompat
import com.example.imagerecognition.data.SerializableFace
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
import com.google.mlkit.vision.objects.ObjectDetector
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.pow
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

fun objectDetector(): ObjectDetector {
    val mlKitObjectDetection =
        ObjectDetectorOptions.Builder().setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableMultipleObjects()
            .enableClassification().build()
    val objectDetector = ObjectDetection.getClient(mlKitObjectDetection)
    return objectDetector
}

fun isValidFace(face: Face): Boolean {
    return face.getLandmark(FaceLandmark.LEFT_EYE) != null &&
            face.getLandmark(FaceLandmark.RIGHT_EYE) != null &&
            face.getLandmark(FaceLandmark.NOSE_BASE) != null
}

suspend fun convertBitmapToFace(bitmap: Bitmap): Face? =
    suspendCancellableCoroutine { cont ->
        val inputImage = InputImage.fromBitmap(bitmap, 0)

        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setMinFaceSize(0.5f)
            .enableTracking()
            .build()

        val faceDetector = FaceDetection.getClient(options)

        faceDetector.process(inputImage)
            .addOnSuccessListener { faces ->
                if (faces.isNotEmpty()) {
                    cont.resume(faces.first())
                } else {
                    cont.resumeWith(Result.success(null))
                }
            }
            .addOnFailureListener { exception ->
                cont.resumeWithException(exception)
            }
    }

fun detectFaceFeatures(
    bitmap: Bitmap, onFeaturesExtracted: (List<Face>) -> Unit,
    onError: (Exception) -> Unit
) {
    val inputImage = InputImage.fromBitmap(bitmap, 0)
    val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .enableTracking()
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

fun calculateLandmarkSimiliarity(registeredFace: Face, liveFace: Face): Float {
    val registeredFaceLandmark = listOf(
        registeredFace.getLandmark(FaceLandmark.LEFT_EYE)?.position,
        registeredFace.getLandmark(FaceLandmark.RIGHT_EYE)?.position,
        registeredFace.getLandmark(FaceLandmark.NOSE_BASE)?.position,
    )

    val liveFaceLandmark = listOf(
        liveFace.getLandmark(FaceLandmark.LEFT_EYE)?.position,
        liveFace.getLandmark(FaceLandmark.RIGHT_EYE)?.position,
        liveFace.getLandmark(FaceLandmark.NOSE_BASE)?.position,
    )

    val distances = registeredFaceLandmark.zip(liveFaceLandmark).mapNotNull { (registered, live) ->
        if (registered != null && live != null) {
            sqrt((registered.x - live.x).pow(2) + (registered.y - live.y).pow(2))
        } else {
            null
        }
    }

    return distances.average().toFloat()
}

fun compareLandmarks(registeredFace: Face, liveFace: Face): Boolean {

    val registeredPhotoEye = registeredFace.getLandmark(FaceLandmark.LEFT_EYE)?.position
    val livePhotoEye = liveFace.getLandmark(FaceLandmark.LEFT_EYE)?.position

    if (registeredPhotoEye != null && livePhotoEye != null) {
        val dx = registeredPhotoEye.x - livePhotoEye.x
        val dy = registeredPhotoEye.y - livePhotoEye.y
        val distance = sqrt(dx * dx + dy * dy)

        val threshold = 10
        return distance < threshold
    }

    return false
}


fun cosineSimilarity(embedding1: FloatArray, embedding2: FloatArray): Float {
    val dotProduct = embedding1.zip(embedding2) { a, b -> a * b }.sum()
    val norm1 = sqrt(embedding1.map { it * it }.sum().toDouble())
    val norm2 = sqrt(embedding2.map { it * it }.sum().toDouble())
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

fun Face.toSerializableFace(): SerializableFace {
    return SerializableFace(
        boundingBox = "${boundingBox.left},${boundingBox.top},${boundingBox.right},${boundingBox.bottom}",
        trackingId = trackingId,
        smileProbability = smilingProbability,
        leftEyeOpenProbability = leftEyeOpenProbability,
        rightEyeOpenProbability = rightEyeOpenProbability
    )
}

fun SerializableFace.toFace(): Face {
    throw UnsupportedOperationException("Cannot convert SerializableFace back to Face directly")
}

fun serializeFaces(faces: List<Face>): String {
    val serializableFaces = faces.map { it.toSerializableFace() }
    return Json.encodeToString(serializableFaces)
}

fun deserializableFaceData(json: String): List<SerializableFace> {
    return Json.decodeFromString(json)
}

fun serializeBitmap(bitmap: Bitmap): String {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    val byteArray = outputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
}

fun deserializeBitmap(base64: String): Bitmap {
    val byteArray = Base64.decode(base64, Base64.DEFAULT)
    return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
}