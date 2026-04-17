package com.pigs.borrowit.screens.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.pigs.borrowit.ui.theme.Primary

@Composable
fun EditCommDialog(
    initialName: String,
    initialDescription: String,
    initialBannerUrl: String?,
    initialProfileUrl: String?,
    onDismiss: () -> Unit,
    onSave: (String, String, String?, String?) -> Unit,
    onDelete: () -> Unit
) {
    var commName by remember { mutableStateOf(initialName) }
    var commDescription by remember { mutableStateOf(initialDescription) }
    var profileImageUri by remember { mutableStateOf(initialProfileUrl) }
    var bannerImageUri by remember { mutableStateOf(initialBannerUrl) }
    
    var nameError by remember { mutableStateOf("") }
    var descriptionError by remember { mutableStateOf("") }
    var attemptedSubmit by remember { mutableStateOf(false) }
    var shouldShake by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val profileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        profileImageUri = uri?.toString()
    }

    val bannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        bannerImageUri = uri?.toString()
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Community") },
            text = { Text("Are you sure you want to delete this community? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit Community",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                ) {
                    // Banner Selection
                    Text(
                        text = "Community Banner",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.LightGray.copy(alpha = 0.3f))
                            .clickable { bannerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (bannerImageUri != null && bannerImageUri != "null") {
                            AsyncImage(
                                model = bannerImageUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Profile Image Selection
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray.copy(alpha = 0.3f))
                                .border(2.dp, Primary, CircleShape)
                                .clickable { profileLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (profileImageUri != null && profileImageUri != "null") {
                                AsyncImage(
                                    model = profileImageUri,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Groups,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Change profile image",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Name Field
                    OutlinedTextField(
                        value = commName,
                        onValueChange = {
                            commName = it
                            if (attemptedSubmit) nameError = if (it.isBlank()) "Name is required" else ""
                        },
                        label = { Text("Community Name") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = nameError.isNotEmpty(),
                        supportingText = { 
                            if (nameError.isNotEmpty()) {
                                Text(text = nameError)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            focusedLabelColor = Primary
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Description Field
                    OutlinedTextField(
                        value = commDescription,
                        onValueChange = {
                            commDescription = it
                            if (attemptedSubmit) descriptionError = if (it.isBlank()) "Description is required" else ""
                        },
                        label = { Text("Description") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        singleLine = false,
                        isError = descriptionError.isNotEmpty(),
                        supportingText = {
                            if (descriptionError.isNotEmpty()) {
                                Text(text = descriptionError)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            focusedLabelColor = Primary
                        )
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Danger Zone
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Danger Zone",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete Community")
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Save Button
                val offsetX = remember { Animatable(0f) }
                LaunchedEffect(shouldShake) {
                    if (shouldShake) {
                        repeat(4) {
                            offsetX.animateTo(10f, animationSpec = tween(50))
                            offsetX.animateTo(-10f, animationSpec = tween(50))
                        }
                        offsetX.animateTo(0f, animationSpec = tween(50))
                        shouldShake = false
                    }
                }

                Button(
                    onClick = {
                        attemptedSubmit = true
                        val isNameValid = commName.isNotBlank()
                        val isDescValid = commDescription.isNotBlank()
                        
                        if (isNameValid && isDescValid) {
                            onSave(commName, commDescription, bannerImageUri, profileImageUri)
                            onDismiss()
                        } else {
                            nameError = if (!isNameValid) "Name is required" else ""
                            descriptionError = if (!isDescValid) "Description is required" else ""
                            shouldShake = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .height(56.dp)
                        .graphicsLayer { translationX = offsetX.value },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
