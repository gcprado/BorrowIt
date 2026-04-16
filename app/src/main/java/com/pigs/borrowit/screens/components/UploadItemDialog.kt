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
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadItemDialog(
    onDismiss: () -> Unit
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
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

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
                modifier = Modifier
                    .fillMaxSize()
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
                    onDismiss()
                },
                onDismiss = { showConfirmation = false }
            )
        }
    }
}

@Composable
fun DialogHeader(onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Añadir nuevo objeto",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onDismiss) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Cerrar"
            )
        }
    }
}

@Composable
fun SingleLineTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    isError: Boolean = false,
    errorMessage: String = "",
    isRequired: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = if (isRequired) "$label *" else label,
            fontSize = 16.sp,
            color = if (isError) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = { newValue -> onValueChange(newValue) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = if (placeholder.isNotEmpty()) {
                { Text(placeholder) }
            } else null,
            isError = isError,
            trailingIcon = trailingIcon,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                errorBorderColor = MaterialTheme.colorScheme.error
            )
        )
        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun TextArea(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    minLines: Int = 3,
    maxLines: Int = 5,
    isError: Boolean = false,
    errorMessage: String = "",
    enabled: Boolean = true
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = if (isError) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.padding(bottom = 4.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = { newValue -> onValueChange(newValue) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = if (placeholder.isNotEmpty()) {
                { Text(placeholder) }
            } else null,
            minLines = minLines,
            maxLines = maxLines,
            singleLine = false,
            isError = isError,
            enabled = enabled,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                errorBorderColor = MaterialTheme.colorScheme.error
            )
        )
        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
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
    val conditions = listOf("Nuevo", "Usado", "Excelente estado", "Buen estado", "Estado aceptable")
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Condición *",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = if (errorMessage.isNotEmpty()) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            conditions.forEach { condition ->
                FilterChip(
                    selected = selectedCondition == condition,
                    onClick = { onConditionSelected(condition) },
                    label = { Text(condition) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
        
        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp)
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
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onAddImage(it.toString()) }
    }
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Imágenes *",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = if (errorMessage.isNotEmpty()) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Agregar foto",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Agregar\nfotos",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            imageUris.take(2).forEachIndexed { index, uri ->
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    AsyncImage(
                        model = uri,
                        contentDescription = "Imagen ${index + 1}",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = { onRemoveImage(index) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(24.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Eliminar",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
        
        if (imageUris.isNotEmpty()) {
            Text(
                text = "${imageUris.size} imagen${if (imageUris.size > 1) "es" else ""} seleccionada${if (imageUris.size > 1) "s" else ""}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        
        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp)
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
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    val startDatePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            onStartDateSelected(dateFormat.format(calendar.time))
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    
    val endDatePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            onEndDateSelected(dateFormat.format(calendar.time))
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Disponibilidad *",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = if (errorMessage.isNotEmpty()) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DateField(
                label = "Fecha inicio",
                value = startDate,
                onClick = { startDatePickerDialog.show() },
                modifier = Modifier.weight(1f),
                isError = errorMessage.isNotEmpty()
            )
            
            DateField(
                label = "Fecha fin",
                value = endDate,
                onClick = { endDatePickerDialog.show() },
                modifier = Modifier.weight(1f),
                isError = errorMessage.isNotEmpty()
            )
        }
        
        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun DateField(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = if (isError) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent
            ),
            border = if (isError) {
                BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            } else {
                ButtonDefaults.outlinedButtonBorder
            },
            contentPadding = PaddingValues(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = value.ifEmpty { "Seleccionar" },
                fontSize = 14.sp,
                color = if (value.isEmpty()) 
                    MaterialTheme.colorScheme.onSurfaceVariant 
                else 
                    MaterialTheme.colorScheme.onSurface
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
            val shakeKeyframes = listOf(0f, -10f, 10f, -10f, 10f, -5f, 5f, 0f)
            shakeKeyframes.forEach { offset ->
                offsetX.animateTo(
                    targetValue = offset,
                    animationSpec = tween(durationMillis = 50)
                )
            }
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
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "¡Publicación exitosa!",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text("Tu objeto ha sido publicado correctamente.")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Aceptar")
            }
        },
        shape = RoundedCornerShape(12.dp)
    )
}

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
