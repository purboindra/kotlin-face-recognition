package com.example.imagerecognition

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.imagerecognition.utils.compareFaces
import com.example.imagerecognition.utils.loadBitmapFromFile
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
    private val _bitmap = MutableStateFlow<List<Bitmap>>(emptyList())
    val bitmap = _bitmap.asStateFlow()

    private val _loadingImage = MutableStateFlow<Boolean>(false)
    val loadingImage = _loadingImage.asStateFlow()

    private val _isLivePhoto = MutableStateFlow<Boolean>(false)
    val isLivePhoto = _isLivePhoto.asStateFlow()

    private val _isLoadingIdentify = MutableStateFlow<Boolean>(false)
    val isLoadingIdentify = _isLoadingIdentify.asStateFlow()

    fun changeLoading(value: Boolean) {
        _loadingImage.value = value
        println("Loading state: ${_loadingImage.value}")
    }

    fun setIsLivePhoto(value: Boolean) {
        _isLivePhoto.value = value
    }

    fun onTakePhoto(bitmap: Bitmap) {
        _bitmap.value += bitmap
        println("onTapPhoto: ${_bitmap.value}")
        _loadingImage.value = false
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

    private suspend fun convertBitmapToFace(bitmap: Bitmap): Face? = suspendCancellableCoroutine {
        val inputImage = InputImage.fromBitmap(bitmap, 0)

        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .enableTracking()
            .build()

        val faceDetector = FaceDetection.getClient(options)

        faceDetector.process(inputImage).addOnSuccessListener { faces ->
            if (faces.isNotEmpty()) {
                val face = faces.first()
                println("Face detected: $face")
            }
        }.addOnFailureListener { exception ->
            println("Face detection failed: $exception")
        }
    }

    fun onCompareFaces(bitmap: Bitmap?) {
        _isLoadingIdentify.value = true;
        bitmap?.let {
            viewModelScope.launch {
                val face = try {
                    convertBitmapToFace(it)
                } catch (e: Exception) {
                    println("Face detection failed: ${e.message}")
                    null
                }

                println("on compare faces: ${face?.boundingBox}")
            }
        }
        _isLoadingIdentify.value = false;
    }
}