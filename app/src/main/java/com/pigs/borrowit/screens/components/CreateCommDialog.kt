package com.pigs.borrowit.screens.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.pigs.borrowit.ui.theme.Primary

@Composable
fun CreateCommDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, String?, String?) -> Unit
) {
    var commName by remember { mutableStateOf("") }
    var commDescription by remember { mutableStateOf("") }
    var profileImageUri by remember { mutableStateOf<String?>(null) }
    var bannerImageUri by remember { mutableStateOf<String?>(null) }
    
    var nameError by remember { mutableStateOf("") }
    var descriptionError by remember { mutableStateOf("") }
    var attemptedSubmit by remember { mutableStateOf(false) }
    var shouldShake by remember { mutableStateOf(false) }

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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
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
                        text = "Crear Comunidad",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
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
                        text = "Banner de la comunidad",
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
                        if (bannerImageUri != null) {
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
                            if (profileImageUri != null) {
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
                            text = "Imagen de perfil\n(opcional)",
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
                            if (attemptedSubmit) nameError = if (it.isBlank()) "El nombre es obligatorio" else ""
                        },
                        label = { Text("Nombre de la comunidad") },
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
                            if (attemptedSubmit) descriptionError = if (it.isBlank()) "La descripción es obligatoria" else ""
                        },
                        label = { Text("Descripción") },
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

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Create Button
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
                            onCreate(commName, commDescription, bannerImageUri, profileImageUri)
                            onDismiss()
                        } else {
                            nameError = if (!isNameValid) "El nombre es obligatorio" else ""
                            descriptionError = if (!isDescValid) "La descripción es obligatoria" else ""
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
                    Text("Crear Comunidad", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
