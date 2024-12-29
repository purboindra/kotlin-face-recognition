package com.example.imagerecognition.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.media.Image
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.imagerecognition.data.State
import com.example.imagerecognition.utils.bitmapToUri
import com.example.imagerecognition.utils.convertBitmapToFace
import com.example.imagerecognition.utils.convertBitmapToInputImage
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.Serializable
import java.io.File
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class User(
    val imageUrl: String = "",
    val username: String = "",
    val id: String = "",
)

@Serializable
data class ImageLabel(
    val label: String,
    val confidence: Float
)

@Serializable
data class RegisterResponse<out T>(val data: T? = null, val message: String? = null)

@HiltViewModel
class RegisterViewModel @Inject constructor() : ViewModel() {
    
    private val databaseUrl = "gs://add-project-1fd3c.appspot.com"
    private val bucketImage = "face-recognition"
    
    private val _imageLabelsState =
        MutableStateFlow<List<com.example.imagerecognition.ui.viewmodel.ImageLabel>>(emptyList())
    val imageLabelsState = _imageLabelsState.asStateFlow()
    
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
    
    private suspend fun storeUserToDatabase(userId: String, user: User) {
        val db = Firebase.firestore
        
        try {
            db.collection("users").document(userId).set(user).await()
            
            println("DocumentSnapshot successfully written!")
        } catch (e: Throwable) {
            println("Error storing user to database: ${e.message}")
        }
        
    }
    
    @OptIn(ExperimentalUuidApi::class)
    fun register(context: Context) {
        _registerState.value = State.Loading
        
        // Reset _imageLabelsState
        _imageLabelsState.value = emptyList()
        
        viewModelScope.launch {
            try {
                val face = try {
                    convertBitmapToFace(_registeredPhotoBitmap.value!!)
                } catch (e: Throwable) {
                    val throwable = Throwable("Face conversion failed: ${e.message}")
                    _registerState.value = State.Error(throwable)
                    return@launch
                }
                
                if (face == null) {
                    try {
                        val registerInputImage =
                            convertBitmapToInputImage(_registeredPhotoBitmap.value!!)
                        val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
                        labeler.process(registerInputImage).addOnSuccessListener { labels ->
                            for (label in labels) {
                                println("Label: ${label.text}, confidence: ${label.confidence}")
                                _imageLabelsState.value += ImageLabel(
                                    label.text,
                                    label.confidence
                                )
                            }
                        }.addOnFailureListener { throwable ->
                            println("Labeler error: ${throwable.message}")
                            _registerState.value = State.Error(throwable)
                            return@addOnFailureListener
                        }
                        
                        val throwable = Throwable("Face not valid")
                        _registerState.value = State.Error(throwable)
                        return@launch
                    } catch (e: Throwable) {
                        println("Register failed: ${e.message}")
                        _registerState.value = State.Error(e)
                        return@launch
                    }
                }
                
                val registeredUri = bitmapToUri(context, _registeredPhotoBitmap.value!!)
                
                val storage = Firebase.storage(databaseUrl)
                val storageRef = storage.reference
                val imageRef = storageRef.child("$bucketImage/${_username.value}.png")
                
                val file = File(registeredUri.path!!)
                
                imageRef.putFile(registeredUri).await()
                file.delete()
                
                val imageUrl = imageRef.downloadUrl.await().toString()
                val user = User(
                    imageUrl = imageUrl,
                    username = _username.value,
                    id = Uuid.random().toString()
                )
                
                storeUserToDatabase(user.id, user)
                
                _registerState.value = State.Success(RegisterResponse(data = true))
                
            } catch (e: Exception) {
                println("Register failed: ${e.message}")
                _registerState.value = State.Error(e)
            }
        }
    }
}