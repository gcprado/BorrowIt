package com.pigs.borrowit.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.pigs.borrowit.R
import com.pigs.borrowit.control.ProfileViewModel
import com.pigs.borrowit.presentation.navigation.GraphRoute
import com.pigs.borrowit.presentation.navigation.Screen
import com.pigs.borrowit.screens.components.HistoryDialog
import com.pigs.borrowit.screens.components.MainBottomNav
import com.pigs.borrowit.ui.theme.Background
import com.pigs.borrowit.ui.theme.CardBackground
import com.pigs.borrowit.ui.theme.Primary
import com.pigs.borrowit.ui.theme.PrimaryDark
import com.pigs.borrowit.ui.theme.PrimaryLight
import com.pigs.borrowit.utils.ImageUtils

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
    val scrollState = rememberScrollState()
    var showHistory by remember { mutableStateOf(false) }

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
        uri?.let { viewModel.uploadImage(context, it, ImageUtils::compressImage) }
    }

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "My Profile",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = PrimaryDark
                ),
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Profile Picture Section
            Box(
                modifier = Modifier.size(140.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                val profilePicUrl = userState?.profilePicture ?: ""

                Surface(
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    shape = CircleShape,
                    color = PrimaryLight,
                    border = BorderStroke(4.dp, Color.White),
                    shadowElevation = 4.dp
                ) {
                    if (profilePicUrl.isNotEmpty()) {
                        AsyncImage(
                            model = profilePicUrl,
                            contentDescription = "Profile Image",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.profilepicture_default),
                            contentDescription = "Default Profile Image",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Surface(
                    onClick = { if (!isUploadingImage) galleryLauncher.launch("image/*") },
                    shape = CircleShape,
                    color = Primary,
                    modifier = Modifier
                        .size(42.dp)
                        .border(2.dp, Color.White, CircleShape),
                    shadowElevation = 6.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isUploadingImage) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Change Photo",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Username Editable Field
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                placeholder = { Text("Enter your username") },
                textStyle = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(0.85f),
                enabled = !isSaving,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.White.copy(alpha = 0.5f),
                    unfocusedContainerColor = Color.Transparent
                )
            )

            Text(
                text = viewModel.email,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Static Location
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Madrid, Spain",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = PrimaryDark
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Options List
            ProfileOption(
                icon = Icons.Default.Inventory2,
                title = "Manage items",
                onClick = { navController.navigate(Screen.ManageItems.route) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            ProfileOption(
                icon = Icons.Default.History,
                title = "History",
                onClick = { showHistory = true }
            )

            Spacer(modifier = Modifier.height(12.dp))

            ProfileOption(
                icon = Icons.Default.Save,
                title = "Save changes",
                onClick = { viewModel.saveUsername(username) },
                isLoading = isSaving
            )

            Spacer(modifier = Modifier.height(12.dp))

            ProfileOption(
                icon = Icons.AutoMirrored.Filled.Logout,
                title = "Log out",
                onClick = { viewModel.onLogout(context) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            ProfileOption(
                icon = Icons.Default.DeleteForever,
                title = "Delete account",
                onClick = { viewModel.onDeleteAccount() },
                color = Color(0xFFD32F2F)
            )

            Spacer(modifier = Modifier.height(100.dp))
        }

        // Snackbar
        snackbarMessage?.let { message ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 90.dp)
                    .padding(horizontal = 16.dp),
                action = {
                    TextButton(onClick = { viewModel.clearSnackbar() }) {
                        Text("Dismiss", color = Primary)
                    }
                },
                containerColor = Color(0xFF333333),
                contentColor = Color.White
            ) {
                Text(message)
            }
        }

        MainBottomNav(navController, modifier = Modifier.align(Alignment.BottomCenter))

        if (showHistory) {
            HistoryDialog(onDismiss = { showHistory = false })
        }
    }
}

@Composable
private fun ProfileOption(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    color: Color = Color.Black,
    isLoading: Boolean = false
) {
    Surface(
        onClick = if (isLoading) ({}) else onClick,
        color = CardBackground,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        tonalElevation = 2.dp,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Primary,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (color == Color.Black) Primary else color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = color
                ),
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.Gray.copy(alpha = 0.3f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
