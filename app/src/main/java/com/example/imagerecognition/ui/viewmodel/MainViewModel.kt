package com.example.imagerecognition.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import androidx.camera.core.CameraSelector
import androidx.camera.view.CameraController
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.imagerecognition.utils.compareFaces
import com.example.imagerecognition.utils.compareLandmarks
import com.example.imagerecognition.utils.detectFaceFeatures
import com.example.imagerecognition.utils.loadBitmapFromFile
import com.example.imagerecognition.utils.objectDetector
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
    private val _registeredPhotoBitmap = MutableStateFlow<Bitmap?>(null)
    val registeredPhotoBitmap = _registeredPhotoBitmap.asStateFlow()
    
    private val _livePhotoBitmap = MutableStateFlow<Bitmap?>(null)
    val livePhotoBitmap = _livePhotoBitmap.asStateFlow()
    
    private val _loadingImage = MutableStateFlow<Boolean>(false)
    val loadingImage = _loadingImage.asStateFlow()
    
    private val _isLivePhoto = MutableStateFlow<Boolean>(false)
    val isLivePhoto = _isLivePhoto.asStateFlow()
    
    private val _isLoadingIdentify = MutableStateFlow<Boolean>(false)
    val isLoadingIdentify = _isLoadingIdentify.asStateFlow()
    
    private val _isUsingBackCamera = MutableStateFlow<Boolean>(false)
    
    private val _registeredFaceNotDetected = MutableStateFlow<String?>(null)
    val registeredFaceNotDetected = _registeredFaceNotDetected.asStateFlow()
    
    private val _liveFaceNotDetected = MutableStateFlow<String?>(null)
    val liveFaceNotDetected = _liveFaceNotDetected.asStateFlow()
    
    private val _registeredFaces = MutableStateFlow<List<Face>>(emptyList())
    val registeredFaces = _registeredFaces.asStateFlow()
    
    fun toggleCamera(cameraController: CameraController) {
        _isUsingBackCamera.value = !_isUsingBackCamera.value
        cameraController.cameraSelector = if (_isUsingBackCamera.value) {
            CameraSelector.DEFAULT_BACK_CAMERA
        } else {
            CameraSelector.DEFAULT_FRONT_CAMERA
        }
    }
    
    
    fun changeLoading(value: Boolean) {
        _loadingImage.value = value
        println("Loading state: ${_loadingImage.value}")
    }
    
    fun setIsLivePhoto(value: Boolean) {
        _isLivePhoto.value = value
    }
    
    fun onTakePhoto(bitmap: Bitmap, isLivePhoto: Boolean = false) {
        if (isLivePhoto) {
            _livePhotoBitmap.value = bitmap
        } else {
            _registeredPhotoBitmap.value = bitmap
        }
    }
    
    fun onLoadImageBitmapFromFile(context: Context) {
        changeLoading(true)
        val loadImageBitmap = loadBitmapFromFile("registered_face.png", context)
        loadImageBitmap?.let {
            onTakePhoto(it)
        }.also {
            changeLoading(false)
        }
    }
    
    private suspend fun convertBitmapToFace(bitmap: Bitmap): Face? =
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
    
    
    fun onCompareFaces(registeredPhoto: Bitmap?, livePhoto: Bitmap?) {
        viewModelScope.launch {
            _isLoadingIdentify.value = true;
            
            // Reset error message
            _registeredFaceNotDetected.value = null
            _liveFaceNotDetected.value = null
            
            val faceRegistered = registeredPhoto?.let {
                try {
                    convertBitmapToFace(it)
                } catch (e: Exception) {
                    println("Face registered detection failed: ${e.message}")
                    null
                }
            }
            
            val faceLivePhoto = livePhoto?.let {
                try {
                    convertBitmapToFace(it)
                } catch (e: Exception) {
                    println("Face live photo detection failed: ${e.message}")
                    null
                }
            }
            
            println("result convert to face: $faceRegistered $faceLivePhoto")
            
            if (registeredPhoto == null) {
                _registeredFaceNotDetected.value = "Face live detection failed: No face detected"
            } else {
                detectFaceFeatures(
                    registeredPhoto,
                    { faces ->
                        println("detectFaceFeatures: Registered face features: $faces")
                        _registeredFaces.value = faces
                    },
                    { e ->
                        println("detectFaceFeatures: Registered face features detection failed: ${e.message}")
                    }
                )
            }
            
            if (faceLivePhoto == null) {
                _liveFaceNotDetected.value = "Face live detection failed: No face detected"
            }
            
            // Object detection live photo
            val liveImage = InputImage.fromBitmap(livePhoto!!, 0)
            objectDetector().process(liveImage)
                .addOnSuccessListener { detectedObjects ->
                    if (detectedObjects.isEmpty()) {
                        println("No objects detected.")
                    } else {
                        println("Detected objects count: ${detectedObjects.size}")
                        for (detectedObject in detectedObjects) {
                            val boundingBox = detectedObject.boundingBox
                            println("Bounding Box: $boundingBox")
                            if (detectedObject.labels.isEmpty()) {
                                println("No labels detected for this object.")
                            } else {
                                for (label in detectedObject.labels) {
                                    println("Label: ${label.text}, Confidence: ${label.confidence}")
                                }
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    println("Detection failed: ${e.message}")
                }
            
            
            if (faceRegistered != null && faceLivePhoto != null) {
                println("on compare faces: Registered face bounding box: ${faceRegistered?.boundingBox}")
                println("on compare faces: Live photo face bounding box: ${faceLivePhoto?.boundingBox}")
                val isMatch = compareFaces(faceRegistered, faceLivePhoto)
                val isLandmarkMatch = compareLandmarks(faceRegistered, faceLivePhoto)
                
                println("on compare faces: Faces match: $isMatch")
                println("on compare faces: Faces isLandmarkMatch: $isLandmarkMatch")
            } else {
                println("Face detection failed: One or both faces are null.")
            }
            _isLoadingIdentify.value = false;
        }
    }
}