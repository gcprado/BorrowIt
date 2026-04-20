package com.pigs.borrowit.screens

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.pigs.borrowit.R
import com.pigs.borrowit.control.ProfileViewModel
import com.pigs.borrowit.presentation.navigation.GraphRoute
import com.pigs.borrowit.screens.components.MainBottomNav
import com.pigs.borrowit.ui.theme.Background
import com.pigs.borrowit.ui.theme.CardBackground
import com.pigs.borrowit.ui.theme.Primary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

private suspend fun compressImage(
    context: Context,
    uri: Uri,
    maxWidth: Int = 1024,
    quality: Int = 80
): ByteArray? {
    return withContext(Dispatchers.IO) {
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            val scaledBitmap = if (bitmap.width > maxWidth) {
                val scale = maxWidth.toFloat() / bitmap.width
                val newHeight = (bitmap.height * scale).toInt()
                Bitmap.createScaledBitmap(bitmap, maxWidth, newHeight, true)
            } else {
                bitmap
            }
            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            outputStream.toByteArray()
        } catch (e: Exception) {
            Log.e("ProfileScreen", "Error comprimiendo imagen", e)
            null
        }
    }
}

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {
    val userState by viewModel.userState.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val isUploadingImage by viewModel.isUploadingImage.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val navigateToLogin by viewModel.navigateToLogin.collectAsState()

    var username by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(userState) {
        userState?.let { user ->
            if (username.isEmpty()) {
                username = user.username
            }
        }
    }

    LaunchedEffect(navigateToLogin) {
        if (navigateToLogin) {
            navController.navigate(GraphRoute.AUTH) {
                popUpTo(0)
            }
            viewModel.onNavigatedToLogin()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.uploadImage(context, it, ::compressImage) }
    }

    // UI
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            // Card de perfil
            Surface(
                color = Primary,
                shape = RoundedCornerShape(24.dp),
                tonalElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(120.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        val profilePicUrl = userState?.profilePicture ?: ""

                        Surface(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            shape = CircleShape,
                            color = Color.Transparent,
                            border = BorderStroke(2.dp, Color.LightGray)
                        ) {
                            if (profilePicUrl.isNotEmpty()) {
                                Image(
                                    painter = rememberAsyncImagePainter(profilePicUrl),
                                    contentDescription = "Profile Image",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.profilepicture_default),
                                    contentDescription = "Default Profile Image",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        FloatingActionButton(
                            onClick = { 
                                if (!isUploadingImage) {
                                    galleryLauncher.launch("image/*")
                                }
                            },
                            modifier = Modifier.size(36.dp),
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ) {
                            if (isUploadingImage) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            } else {
                                Icon(Icons.Default.Edit, contentDescription = "Change Photo")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = viewModel.email,
                        fontSize = 14.sp,
                        color = Color.Black.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        placeholder = { Text("Enter your username") },
                        textStyle = LocalTextStyle.current.copy(fontSize = 16.sp),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSaving,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color.Gray,
                            unfocusedBorderColor = Primary,
                            cursorColor = Primary,
                            focusedLabelColor = Primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card de botones
            Surface(
                color = CardBackground,
                shape = RoundedCornerShape(24.dp),
                tonalElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Button(
                        onClick = { viewModel.saveUsername(username) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isSaving && username.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            contentColor = Color.Black
                        )
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save Changes")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.onLogout() },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE5D1B0),
                            contentColor = Color.Black
                        )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Log Out")
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.Logout, contentDescription = null)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.onDeleteAccount() },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF2B8B5),
                            contentColor = Color.Black
                        )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Delete Account")
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.Delete, contentDescription = null)
                        }
                    }
                }
            }
        }

        // Snackbar
        snackbarMessage?.let { message ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp),
                action = {
                    TextButton(onClick = { viewModel.clearSnackbar() }) {
                        Text("Dismiss")
                    }
                }
            ) {
                Text(message)
            }
        }

        MainBottomNav(navController, modifier = Modifier.align(Alignment.BottomCenter))
    }
}
