package com.example.imagerecognition.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.imagerecognition.data.State
import com.example.imagerecognition.utils.bitmapToUri
import com.example.imagerecognition.utils.convertBitmapToFace
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.storage.storage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.io.File
import javax.inject.Inject

@Serializable
data class RegisterResponse<out T>(val data: T? = null, val message: String? = null)

@HiltViewModel
class RegisterViewModel @Inject constructor() : ViewModel() {

    private val _registerState = MutableStateFlow<State<RegisterResponse<Boolean>>>(State.Idle)
    val registerState = _registerState.asStateFlow()

    private val _registeredPhotoBitmap = MutableStateFlow<Bitmap?>(null)
    val registeredPhotoBitmap = _registeredPhotoBitmap.asStateFlow()

    private val _isLoadingRegister = MutableStateFlow<Boolean>(false)
    val isLoadingRegister = _isLoadingRegister.asStateFlow()

    private val _username = MutableStateFlow<String>("")
    val username = _username.asStateFlow()

    private fun changeLoading(value: Boolean) {
        _isLoadingRegister.value = value
    }

    fun setRegisteredPhoto(bitmap: Bitmap) {
        _registeredPhotoBitmap.value = bitmap
    }

    fun setUsername(username: String) {
        _username.value = username
    }

    fun register(context: Context) {
        _registerState.value = State.Loading
        viewModelScope.launch {
            try {
                val face = try {
                    convertBitmapToFace(_registeredPhotoBitmap.value!!)
                } catch (e: Throwable) {
                    println("Face conversion failed: ${e.message}")
                    val throwable = Throwable("Face conversion failed: ${e.message}")
                    _registerState.value = State.Error(throwable)
                    return@launch
                }

                if (face == null) {
                    val throwable = Throwable("Face not valid")
                    _registerState.value = State.Error(throwable)
                    return@launch
                }

                val registeredUri = bitmapToUri(context, _registeredPhotoBitmap.value!!)

                val storage = Firebase.storage("gs://add-project-1fd3c.appspot.com")
                val storageRef = storage.reference
                val imageRef = storageRef.child("face-recognition/${_username.value}.png")

                val file = File(registeredUri.path!!)

                imageRef.putFile(registeredUri).addOnSuccessListener {
                    println("Image uploaded")
                    file.delete()
                    changeLoading(false)
                }.addOnFailureListener {
                    println("Image upload failed: ${it.message}")
                    file.delete()
                    changeLoading(false)
                }
            } catch (e: Exception) {
                println("Register failed: ${e.message}")
                _registerState.value = State.Error(e)
            }
        }
    }
}