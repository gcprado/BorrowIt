package com.pigs.borrowit.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pigs.borrowit.data.models.BorrowItem
import com.pigs.borrowit.data.models.ItemStatus
import com.pigs.borrowit.data.models.toDisplayString
import com.pigs.borrowit.ui.theme.Background
import com.pigs.borrowit.ui.theme.Primary

@Composable
fun ItemDetailDialog(
    item: BorrowItem,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp),
            color = Background
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Detalle del Item",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.Black
                        )
                    }
                }

                Divider(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    color = Color.LightGray,
                    thickness = 1.dp
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(
                                color = Primary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.name.first().toString().uppercase(),
                            fontSize = 80.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = item.name,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )

                            DetailRow(
                                label = "Estado",
                                value = item.status.toDisplayString(),
                                valueColor = when (item.status) {
                                    ItemStatus.AVAILABLE -> Color(0xFF2E7D32)
                                    ItemStatus.BORROWED -> Color(0xFF1976D2)
                                    ItemStatus.IN_USE -> Color(0xFFE65100)
                                    else -> Color.Gray
                                }
                            )

                            Divider(color = Color.LightGray.copy(alpha = 0.5f))

                            DetailRow(
                                label = "Condición",
                                value = item.condition
                            )

                            if (item.description.isNotEmpty()) {
                                Divider(color = Color.LightGray.copy(alpha = 0.5f))
                                
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Descripción",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = item.description,
                                        fontSize = 16.sp,
                                        color = Color.Black
                                    )
                                }
                            }

                            if (item.availableFrom.isNotEmpty() && item.availableUntil.isNotEmpty()) {
                                Divider(color = Color.LightGray.copy(alpha = 0.5f))
                                
                                DetailRow(
                                    label = "Disponible desde",
                                    value = item.availableFrom
                                )
                                
                                DetailRow(
                                    label = "Disponible hasta",
                                    value = item.availableUntil
                                )
                            }

                            if (item.category.isNotEmpty()) {
                                Divider(color = Color.LightGray.copy(alpha = 0.5f))
                                
                                DetailRow(
                                    label = "Categoría",
                                    value = item.category
                                )
                            }

                            if (item.borrowerName != null) {
                                Divider(color = Color.LightGray.copy(alpha = 0.5f))
                                
                                DetailRow(
                                    label = "Prestado a",
                                    value = item.borrowerName
                                )
                            }

                            if (item.ownerName.isNotEmpty()) {
                                Divider(color = Color.LightGray.copy(alpha = 0.5f))
                                
                                DetailRow(
                                    label = "Propietario",
                                    value = item.ownerName
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = Color.Black
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}
