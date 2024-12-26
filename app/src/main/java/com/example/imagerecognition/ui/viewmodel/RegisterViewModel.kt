package com.example.imagerecognition.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.imagerecognition.utils.convertBitmapToFace
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.storage.storage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor() : ViewModel() {

    val firebaseAuth = Firebase.auth
    val firebaseStorage = Firebase.storage

    private val _registeredPhotoBitmap = MutableStateFlow<Bitmap?>(null)
    val registeredPhotoBitmap = _registeredPhotoBitmap.asStateFlow()

    val _isLoadingRegister = MutableStateFlow<Boolean>(false)
    val isLoadingRegister = _isLoadingRegister.asStateFlow()

    fun changeLoading(value: Boolean) {
        _isLoadingRegister.value = value
    }

    fun setRegisteredPhoto(bitmap: Bitmap) {
        _registeredPhotoBitmap.value = bitmap
    }

    fun register() {
        changeLoading(true)
        viewModelScope.launch {
            val face = try {
                convertBitmapToFace(_registeredPhotoBitmap.value!!)
            } catch (e: Throwable) {
                null
            }

            if (face == null) return@launch

            val storage = Firebase.storage("gs://add-project-1fd3c.appspot.com/face-recognition")
            val storageRef = storage.reference
            val imageRef = storageRef.child("${System.currentTimeMillis()}.png")
        }
    }
}