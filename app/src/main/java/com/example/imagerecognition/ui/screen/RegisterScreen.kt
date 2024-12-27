package com.example.imagerecognition.ui.screen

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {

                if (registeredImage != null) {
                    Image(
                        bitmap = registeredImage!!.asImageBitmap(),
                        contentDescription = "Image Preview",
                        modifier = Modifier
                            .width(102.dp)
                            .height(102.dp)
                            .clip(CircleShape)
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

        }
    }
}