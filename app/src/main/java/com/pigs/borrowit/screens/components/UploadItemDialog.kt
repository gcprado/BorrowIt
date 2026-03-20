package com.pigs.borrowit.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun UploadItemDialog(
    onDismiss: () -> Unit
) {

    var itemName by remember { mutableStateOf("") }
    var itemDescription by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss
    ) {

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .background(Color.White)

            ) {
                SingleLineTextField(
                    label = "Nombre del objeto",
                    value = itemName,
                    onValueChange = { itemName = it },
                    placeholder = "Secador de pelo de mano con mango de plástico...",
                )
                TextArea(
                    label = "Descripción del objeto",
                    value = itemDescription,
                    onValueChange = { itemDescription = it },
                    placeholder = "Secador de pelo de mano con mango de plástico...",
                    minLines = 4,
                    maxLines = 8
                )
            }
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
    supportingText: String = "",
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
            supportingText = if (supportingText.isNotEmpty()) {
                { Text(supportingText) }
            } else null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                errorBorderColor = MaterialTheme.colorScheme.error
            )
        )
    }
}

@Composable
fun SingleLineTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    isError: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null
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
    }
}