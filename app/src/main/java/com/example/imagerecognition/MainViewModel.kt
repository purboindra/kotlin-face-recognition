package com.example.imagerecognition

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel : ViewModel() {
    private val _bitmap = MutableStateFlow<List<Bitmap>>(emptyList())
    val bitmap = _bitmap.asStateFlow()
    
    fun onTakePhoto(bitmap: Bitmap) {
        _bitmap.value += bitmap
    }
}