package com.example.imagerecognition.ui.screen

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Observer
import androidx.navigation.NavHostController
import com.example.imagerecognition.data.State
import com.example.imagerecognition.ui.viewmodel.RegisterViewModel

@Composable
fun RegisterScreen(
    registerViewModel: RegisterViewModel = hiltViewModel<RegisterViewModel>(),
    navHostController: NavHostController
) {
    
    val context = LocalContext.current
    
    val image =
        navHostController.currentBackStackEntry?.savedStateHandle?.getLiveData<String?>("image")
    val registeredImage by registerViewModel.registeredPhotoBitmap.collectAsState()
    val registerState by registerViewModel.registerState.collectAsState()
    val username by registerViewModel.username.collectAsState()
    val imageLabels by registerViewModel.imageLabelsState.collectAsState()
    
    DisposableEffect(image) {
        val observer = Observer<String?> { value ->
            value?.let {
                val decodeBitmap = Base64.decode(it, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodeBitmap, 0, decodeBitmap.size)
                registerViewModel.setRegisteredPhoto(bitmap)
            }
        }
        image?.observeForever(observer)
        onDispose {
            image?.removeObserver(observer)
        }
    }
    
    LaunchedEffect(registerState) {
        when (registerState) {
            is State.Error -> {
                val throwable = ((registerState as State.Error).throwable)
                println("Register error: ${throwable.message}")
                Toast.makeText(context, throwable.message, Toast.LENGTH_LONG).show()
            }
            
            is State.Success -> {
                Toast.makeText(context, "Success register", Toast.LENGTH_LONG).show()
            }
            
            else -> {}
        }
    }
    
    Scaffold(modifier = Modifier.safeContentPadding()) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.Center
        ) {
            
            if (registeredImage != null) {
                Image(
                    bitmap = registeredImage!!.asImageBitmap(),
                    contentDescription = "Image Preview",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(248.dp)
                        .clickable {
                            navHostController.navigate(
                                "camera_preview/${false}"
                            )
                        },
                    contentScale = ContentScale.Crop,
                )
            } else {
                ElevatedButton(
                    onClick = {
                        navHostController.navigate(
                            "camera_preview/${false}"
                        )
                    },
                ) {
                    Text("Take photo")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                
                Spacer(modifier = Modifier.width(10.dp))
                
                OutlinedTextField(
                    value = username,
                    onValueChange = { registerViewModel.setUsername(it) },
                    label = {
                        Text("Username")
                    },
                    shape = RoundedCornerShape(
                        12.dp,
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            ElevatedButton(
                onClick = {
                    registerViewModel.register(context)
                },
                enabled = registerState is State.Loading || registeredImage != null && username.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (registerState is State.Loading) CircularProgressIndicator() else Text("Register")
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            if (registerState is State.Error) Column {
                Text(
                    text = "Error: ${(registerState as State.Error).throwable.message}",
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyColumn {
                    items(imageLabels) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                item.label,
                                modifier = Modifier.width(48.dp),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.Top
                            ) {
                                Text("Confidence", style = MaterialTheme.typography.labelMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.Top) {
                                    
                                    Canvas(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(12.dp)
                                    ) {
                                        drawLine(
                                            color = Color.Gray.copy(0.27f),
                                            start = Offset(0f, size.height / 2),
                                            end = Offset(size.width, size.height / 2),
                                            strokeWidth = size.height
                                        )
                                        
                                        drawLine(
                                            color = Color.Red,
                                            start = Offset(0f, 0f),
                                            end = Offset(item.confidence * size.width, 0f),
                                            strokeWidth = size.height
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "${(item.confidence * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.width(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}