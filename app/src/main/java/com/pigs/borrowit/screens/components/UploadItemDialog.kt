package com.pigs.borrowit.screens.components

import android.app.DatePickerDialog
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.pigs.borrowit.data.model.Availability
import com.pigs.borrowit.data.model.Item
import com.pigs.borrowit.data.repositories.ItemRepository
import com.pigs.borrowit.ui.theme.Primary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

import android.net.Uri
import com.pigs.borrowit.utils.ImageUtils

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import com.pigs.borrowit.data.model.Community
import com.pigs.borrowit.data.repositories.CommunityRepository

import androidx.compose.material3.MenuAnchorType
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Groups

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun UploadItemDialog(
    userId: String,                     // Logged-in user ID
    onDismiss: () -> Unit,
    onItemUploaded: (String) -> Unit = {} // callback with the created document ID
) {
    var itemName by remember { mutableStateOf("") }
    var itemDescription by remember { mutableStateOf("") }
    var selectedCondition by remember { mutableStateOf("") }
    var selectedCommunity by remember { mutableStateOf<Community?>(null) }
    val imageUris = remember { mutableStateListOf<String>() }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var showConfirmation by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf("") }
    var descriptionError by remember { mutableStateOf("") }
    var dateError by remember { mutableStateOf("") }
    var conditionError by remember { mutableStateOf("") }
    var imageError by remember { mutableStateOf("") }
    var communityError by remember { mutableStateOf("") }
    var attemptedSubmit by remember { mutableStateOf(false) }
    var shouldShake by remember { mutableStateOf(false) }

    // Upload status states
    var isUploading by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { ItemRepository() }
    val communityRepository = remember { CommunityRepository() }

    var userCommunities by remember { mutableStateOf<List<Community>>(emptyList()) }
    var isLoadingCommunities by remember { mutableStateOf(true) }

    LaunchedEffect(userId) {
        userCommunities = communityRepository.getUserCommunities(userId)
        isLoadingCommunities = false
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxSize(0.9f),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                DialogHeader(onDismiss = onDismiss)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    CommunitySelector(
                        communities = userCommunities,
                        selectedCommunity = selectedCommunity,
                        onCommunitySelected = {
                            selectedCommunity = it
                            if (attemptedSubmit) {
                                communityError = validateCommunity(it)
                            }
                        },
                        isLoading = isLoadingCommunities,
                        errorMessage = communityError
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    SingleLineTextField(
                        label = "Item Name",
                        value = itemName,
                        onValueChange = {
                            itemName = it
                            if (attemptedSubmit) {
                                nameError = validateName(it)
                            }
                        },
                        placeholder = "Enter item name",
                        isError = nameError.isNotEmpty(),
                        errorMessage = nameError,
                        isRequired = true
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    TextArea(
                        label = "Description",
                        value = itemDescription,
                        onValueChange = {
                            itemDescription = it
                            if (attemptedSubmit) {
                                descriptionError = validateDescription(it)
                            }
                        },
                        placeholder = "Brief description of the item",
                        minLines = 4,
                        maxLines = 6,
                        isError = descriptionError.isNotEmpty(),
                        errorMessage = descriptionError
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    ConditionSelector(
                        selectedCondition = selectedCondition,
                        onConditionSelected = {
                            selectedCondition = it
                            if (attemptedSubmit) {
                                conditionError = validateCondition(it)
                            }
                        },
                        errorMessage = conditionError
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    ImageUploadSection(
                        imageUris = imageUris,
                        onAddImage = { uri ->
                            imageUris.add(uri)
                            if (attemptedSubmit) {
                                imageError = validateImages(imageUris.toList())
                            }
                        },
                        onRemoveImage = { index ->
                            imageUris.removeAt(index)
                            if (attemptedSubmit) {
                                imageError = validateImages(imageUris.toList())
                            }
                        },
                        errorMessage = imageError
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    DateRangeSelector(
                        startDate = startDate,
                        endDate = endDate,
                        onStartDateSelected = {
                            startDate = it
                            if (attemptedSubmit) {
                                dateError = validateDates(it, endDate)
                            }
                        },
                        onEndDateSelected = {
                            endDate = it
                            if (attemptedSubmit) {
                                dateError = validateDates(startDate, it)
                            }
                        },
                        errorMessage = dateError
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }

                PublishButton(
                    onClick = {
                        attemptedSubmit = true
                        nameError = validateName(itemName)
                        descriptionError = validateDescription(itemDescription)
                        dateError = validateDates(startDate, endDate)
                        conditionError = validateCondition(selectedCondition)
                        imageError = validateImages(imageUris.toList())
                        communityError = validateCommunity(selectedCommunity)

                        val hasErrors = listOf(
                            nameError, descriptionError, dateError,
                            conditionError, imageError, communityError
                        ).any { it.isNotEmpty() }

                        if (hasErrors) {
                            shouldShake = true
                            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                                vibratorManager.defaultVibrator
                            } else {
                                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
                            } else {
                                vibrator.vibrate(200)
                            }
                        } else {
                            showConfirmation = true
                        }
                    },
                    modifier = Modifier.padding(24.dp),
                    shouldShake = shouldShake,
                    onShakeComplete = { shouldShake = false }
                )
            }
        }

        if (showConfirmation) {
            ConfirmationDialog(
                onConfirm = {
                    showConfirmation = false
                    scope.launch {
                        isUploading = true
                        uploadError = null
                        try {
                            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            val startDateObj = dateFormat.parse(startDate) ?: throw Exception("Invalid start date")
                            val endDateObj = dateFormat.parse(endDate) ?: throw Exception("Invalid end date")
                            val availability = Availability(startDateObj, endDateObj)
                            
                            // Upload image to Firebase Storage if exists
                            val localUri = imageUris.firstOrNull()
                            val finalPictureUrl = if (localUri != null) {
                                val compressedData = ImageUtils.compressImage(context, Uri.parse(localUri))
                                if (compressedData != null) {
                                    repository.uploadImage(compressedData)
                                } else {
                                    ""
                                }
                            } else {
                                ""
                            }

                            val newItem = Item(
                                name = itemName,
                                description = itemDescription,
                                owner = userId,
                                condition = selectedCondition,
                                picture = finalPictureUrl,
                                availability = availability,
                                communityId = selectedCommunity?.id ?: ""
                            )

                            val result = repository.addItemSuspend(newItem)
                            result.onSuccess { docId ->
                                onItemUploaded(docId)
                                onDismiss()
                            }.onFailure { e ->
                                uploadError = e.message ?: "Unknown error"
                                isUploading = false
                            }
                        } catch (e: Exception) {
                            uploadError = e.message ?: "Format error"
                            isUploading = false
                        }
                    }
                },
                onDismiss = {
                    showConfirmation = false
                    uploadError = null
                },
                isUploading = isUploading,
                errorMessage = uploadError
            )
        }
    }
}

@Composable
fun DialogHeader(onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Upload Item",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )
        )
        IconButton(onClick = onDismiss) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun SingleLineTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isError: Boolean = false,
    errorMessage: String = "",
    isRequired: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (isRequired) {
                Text(text = " *", color = Color.Red, fontSize = 14.sp)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder) },
            singleLine = true,
            isError = isError,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
        if (isError) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun TextArea(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    minLines: Int = 3,
    maxLines: Int = 5,
    isError: Boolean = false,
    errorMessage: String = ""
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder) },
            minLines = minLines,
            maxLines = maxLines,
            isError = isError,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
        if (isError) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ConditionSelector(
    selectedCondition: String,
    onConditionSelected: (String) -> Unit,
    errorMessage: String = ""
) {
    val conditions = listOf("New", "Like New", "Good", "Used", "Fair")
    val isError = errorMessage.isNotEmpty()

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Condition",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            conditions.forEach { condition ->
                FilterChip(
                    selected = selectedCondition == condition,
                    onClick = { onConditionSelected(condition) },
                    label = { Text(condition) },
                    shape = RoundedCornerShape(12.dp), // Consistent with SortButton in CommsScreen
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
        if (isError) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun ImageUploadSection(
    imageUris: List<String>,
    onAddImage: (String) -> Unit,
    onRemoveImage: (Int) -> Unit,
    errorMessage: String = ""
) {
    val isError = errorMessage.isNotEmpty()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onAddImage(it.toString()) }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Images (at least one)",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            imageUris.forEachIndexed { index, uri ->
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                ) {
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(2.dp)
                            .size(20.dp)
                            .background(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { onRemoveImage(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            if (imageUris.size < 5) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(
                            BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add image",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        if (isError) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun DateRangeSelector(
    startDate: String,
    endDate: String,
    onStartDateSelected: (String) -> Unit,
    onEndDateSelected: (String) -> Unit,
    errorMessage: String = ""
) {
    val context = LocalContext.current
    val isError = errorMessage.isNotEmpty()

    fun showDatePicker(currentDate: String, onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        if (currentDate.isNotEmpty()) {
            try {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                sdf.parse(currentDate)?.let { calendar.time = it }
            } catch (e: Exception) {}
        }

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val formattedMonth = String.format("%02d", month + 1)
                val formattedDay = String.format("%02d", dayOfMonth)
                onDateSelected("$formattedDay/$formattedMonth/$year")
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Availability",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { showDatePicker(startDate, onStartDateSelected) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                border = BorderStroke(1.dp, if (startDate.isEmpty()) Color.LightGray else Primary)
            ) {
                Icon(
                    Icons.Default.CalendarToday, 
                    contentDescription = null, 
                    modifier = Modifier.size(16.dp),
                    tint = if (startDate.isEmpty()) Color.Gray else Primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = startDate.ifEmpty { "Start" },
                    fontSize = 14.sp,
                    maxLines = 1,
                    color = if (startDate.isEmpty()) Color.Gray else Primary
                )
            }

            OutlinedButton(
                onClick = { showDatePicker(endDate, onEndDateSelected) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                border = BorderStroke(1.dp, if (endDate.isEmpty()) Color.LightGray else Primary)
            ) {
                Icon(
                    Icons.Default.CalendarToday, 
                    contentDescription = null, 
                    modifier = Modifier.size(16.dp),
                    tint = if (endDate.isEmpty()) Color.Gray else Primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = endDate.ifEmpty { "End" },
                    fontSize = 14.sp,
                    maxLines = 1,
                    color = if (endDate.isEmpty()) Color.Gray else Primary
                )
            }
        }
        if (isError) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun PublishButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shouldShake: Boolean = false,
    onShakeComplete: () -> Unit = {}
) {
    val offsetX = remember { Animatable(0f) }

    LaunchedEffect(shouldShake) {
        if (shouldShake) {
            repeat(4) {
                offsetX.animateTo(10f, animationSpec = tween(50))
                offsetX.animateTo(-10f, animationSpec = tween(50))
            }
            offsetX.animateTo(0f, animationSpec = tween(50))
            onShakeComplete()
        }
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .graphicsLayer {
                translationX = offsetX.value
            },
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Primary
        )
    ) {
        Text(
            text = "Publish",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isUploading: Boolean = false,
    errorMessage: String? = null
) {
    AlertDialog(
        onDismissRequest = { if (!isUploading) onDismiss() },
        title = {
            Text(
                text = if (errorMessage != null) "Error" else "Success!",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            when {
                errorMessage != null -> Text(errorMessage)
                isUploading -> Text("Uploading item...")
                else -> Text("Your item has been published successfully.")
            }
        },
        confirmButton = {
            if (!isUploading) {
                TextButton(onClick = if (errorMessage != null) onDismiss else onConfirm) {
                    Text(
                        text = if (errorMessage != null) "Close" else "OK",
                        color = Primary
                    )
                }
            }
        },
        dismissButton = {
            if (!isUploading && errorMessage == null) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        },
        shape = RoundedCornerShape(12.dp)
    )
}

// Validation functions
fun validateName(name: String): String {
    return when {
        name.isEmpty() -> "Name is required"
        name.length < 4 -> "Name must be at least 4 characters"
        name.length > 80 -> "Name cannot exceed 80 characters"
        else -> ""
    }
}

fun validateDescription(description: String): String {
    return when {
        description.length > 200 -> "Description cannot exceed 200 characters"
        else -> ""
    }
}

fun validateDates(startDate: String, endDate: String): String {
    return when {
        startDate.isEmpty() && endDate.isEmpty() -> "Both dates are required"
        startDate.isEmpty() -> "Start date is required"
        endDate.isEmpty() -> "End date is required"
        else -> {
            try {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val start = dateFormat.parse(startDate)
                val end = dateFormat.parse(endDate)

                if (start != null && end != null && end.before(start)) {
                    "End date cannot be before start date"
                } else {
                    ""
                }
            } catch (e: Exception) {
                "Invalid date format"
            }
        }
    }
}

fun validateCondition(condition: String): String {
    return if (condition.isEmpty()) {
        "Please select the item's condition"
    } else {
        ""
    }
}

fun validateCommunity(community: Community?): String {
    return if (community == null) {
        "Please select a community"
    } else {
        ""
    }
}

fun validateImages(images: List<String>): String {
    return if (images.isEmpty()) {
        "Please add at least one image"
    } else {
        ""
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunitySelector(
    communities: List<Community>,
    selectedCommunity: Community?,
    onCommunitySelected: (Community) -> Unit,
    isLoading: Boolean,
    errorMessage: String = ""
) {
    var expanded by remember { mutableStateOf(false) }
    val isError = errorMessage.isNotEmpty()

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Post to Community",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { if (!isLoading) expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = if (isLoading) "Loading communities..." else selectedCommunity?.name ?: "Select a community",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryEditable, true),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                isError = isError,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                leadingIcon = {
                    if (selectedCommunity?.profileUrl != null) {
                        AsyncImage(
                            model = selectedCommunity.profileUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = Color.Gray
                        )
                    }
                }
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                communities.forEach { community ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (community.profileUrl != null) {
                                    AsyncImage(
                                        model = community.profileUrl,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color.LightGray),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(community.name.take(1), color = Color.White)
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = community.name)
                            }
                        },
                        onClick = {
                            onCommunitySelected(community)
                            expanded = false
                        }
                    )
                }
            }
        }
        if (isError) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}
