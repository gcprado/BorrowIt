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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.pigs.borrowit.data.model.Availability
import com.pigs.borrowit.data.model.Item
import com.pigs.borrowit.data.repositories.ItemRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadItemDialog(
    userId: String,                     // ID del usuario logueado
    onDismiss: () -> Unit,
    onItemUploaded: (String) -> Unit = {} // callback con el ID del documento creado
) {
    var itemName by remember { mutableStateOf("") }
    var itemDescription by remember { mutableStateOf("") }
    var selectedCondition by remember { mutableStateOf("") }
    val imageUris = remember { mutableStateListOf<String>() }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var showConfirmation by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf("") }
    var descriptionError by remember { mutableStateOf("") }
    var dateError by remember { mutableStateOf("") }
    var conditionError by remember { mutableStateOf("") }
    var imageError by remember { mutableStateOf("") }
    var attemptedSubmit by remember { mutableStateOf(false) }
    var shouldShake by remember { mutableStateOf(false) }

    // Estados para la subida
    var isUploading by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { ItemRepository() }

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

                    SingleLineTextField(
                        label = "Nombre del item",
                        value = itemName,
                        onValueChange = {
                            itemName = it
                            if (attemptedSubmit) {
                                nameError = validateName(it)
                            }
                        },
                        placeholder = "Nombre del item",
                        isError = nameError.isNotEmpty(),
                        errorMessage = nameError,
                        isRequired = true
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    TextArea(
                        label = "Descripción",
                        value = itemDescription,
                        onValueChange = {
                            itemDescription = it
                            if (attemptedSubmit) {
                                descriptionError = validateDescription(it)
                            }
                        },
                        placeholder = "Descripción breve del item",
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

                        val hasErrors = listOf(
                            nameError, descriptionError, dateError,
                            conditionError, imageError
                        ).any { it.isNotEmpty() }

                        if (hasErrors) {
                            shouldShake = true
                            vibrateDevice(context)
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
                            val startDateObj = dateFormat.parse(startDate) ?: throw Exception("Fecha inicio inválida")
                            val endDateObj = dateFormat.parse(endDate) ?: throw Exception("Fecha fin inválida")
                            val availability = Availability(startDateObj, endDateObj)
                            val pictureUrl = imageUris.firstOrNull() ?: ""

                            val newItem = Item(
                                name = itemName,
                                description = itemDescription,
                                owner = userId,
                                condition = selectedCondition,
                                picture = pictureUrl,
                                availability = availability
                            )

                            val result = repository.addItemSuspend(newItem)
                            result.onSuccess { docId ->
                                onItemUploaded(docId)
                                onDismiss()
                            }.onFailure { e ->
                                uploadError = e.message ?: "Error desconocido"
                                isUploading = false
                            }
                        } catch (e: Exception) {
                            uploadError = e.message ?: "Error de formato"
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
            text = "Subir Item",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        IconButton(onClick = onDismiss) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Cerrar",
                tint = MaterialTheme.colorScheme.onSurface
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ConditionSelector(
    selectedCondition: String,
    onConditionSelected: (String) -> Unit,
    errorMessage: String = ""
) {
    val conditions = listOf("Nuevo", "Como nuevo", "Buen estado", "Usado", "Aceptable")
    val isError = errorMessage.isNotEmpty()

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Condición",
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
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
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
            text = "Imágenes (al menos una)",
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
                            contentDescription = "Eliminar",
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
                        contentDescription = "Agregar imagen",
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
            text = "Disponibilidad",
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
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = startDate.ifEmpty { "Inicio" },
                    fontSize = 14.sp,
                    maxLines = 1
                )
            }

            OutlinedButton(
                onClick = { showDatePicker(endDate, onEndDateSelected) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = endDate.ifEmpty { "Fin" },
                    fontSize = 14.sp,
                    maxLines = 1
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
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Text(
            text = "Publicar",
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
                text = if (errorMessage != null) "Error" else "¡Publicación exitosa!",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            when {
                errorMessage != null -> Text(errorMessage)
                isUploading -> Text("Subiendo objeto...")
                else -> Text("Tu objeto ha sido publicado correctamente.")
            }
        },
        confirmButton = {
            if (!isUploading) {
                TextButton(onClick = if (errorMessage != null) onDismiss else onConfirm) {
                    Text(if (errorMessage != null) "Cerrar" else "Aceptar")
                }
            }
        },
        dismissButton = {
            if (!isUploading && errorMessage == null) {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        },
        shape = RoundedCornerShape(12.dp)
    )
}

// Funciones de validación
fun validateName(name: String): String {
    return when {
        name.isEmpty() -> "El nombre es obligatorio"
        name.length < 4 -> "El nombre debe tener al menos 4 caracteres"
        name.length > 80 -> "El nombre no puede exceder los 80 caracteres"
        else -> ""
    }
}

fun validateDescription(description: String): String {
    return when {
        description.length > 200 -> "La descripción no puede exceder los 200 caracteres"
        else -> ""
    }
}

fun validateDates(startDate: String, endDate: String): String {
    return when {
        startDate.isEmpty() && endDate.isEmpty() -> "Ambas fechas son obligatorias"
        startDate.isEmpty() -> "La fecha de inicio es obligatoria"
        endDate.isEmpty() -> "La fecha de fin es obligatoria"
        else -> {
            try {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val start = dateFormat.parse(startDate)
                val end = dateFormat.parse(endDate)

                if (start != null && end != null && end.before(start)) {
                    "La fecha de fin no puede ser anterior a la fecha de inicio"
                } else {
                    ""
                }
            } catch (e: Exception) {
                "Formato de fecha inválido"
            }
        }
    }
}

fun validateCondition(condition: String): String {
    return if (condition.isEmpty()) {
        "Debe seleccionar la condición del item"
    } else {
        ""
    }
}

fun validateImages(images: List<String>): String {
    return if (images.isEmpty()) {
        "Debe agregar al menos una imagen"
    } else {
        ""
    }
}

fun vibrateDevice(context: Context) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(200)
    }
}
