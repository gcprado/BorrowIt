package com.pigs.borrowit.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.firebase.auth.FirebaseAuth
import com.pigs.borrowit.data.repositories.BorrowRepository
import java.text.SimpleDateFormat
import java.util.Locale

import com.pigs.borrowit.ui.theme.Background
import com.pigs.borrowit.ui.theme.Primary

enum class InteractionType {
    LENT, BORROWED;
    
    fun toDisplayString() = when(this) {
        LENT -> "Lent to"
        BORROWED -> "Borrowed from"
    }
}

data class HistoryTransaction(
    val id: String,
    val itemName: String,
    val userName: String,
    val date: String,
    val interactionType: InteractionType
)

enum class ItemsTab {
    MY_ITEMS, BORROWED_ITEMS
}

object MockData {
    val historyTransactions = listOf(
        HistoryTransaction("1", "Drill", "John Doe", "Oct 12", InteractionType.BORROWED),
        HistoryTransaction("2", "Mountain Bike", "Sarah Smith", "Oct 10", InteractionType.LENT),
        HistoryTransaction("3", "Board Game", "Mike Ross", "Oct 05", InteractionType.BORROWED),
        HistoryTransaction("4", "Camping Tent", "Anna Bell", "Sep 28", InteractionType.LENT)
    )
    
    val myItems = listOf(
        BorrowItem("1", "Mountain Bike", ItemStatus.LENT, "Sarah"),
        BorrowItem("2", "Camping Tent", ItemStatus.AVAILABLE),
        BorrowItem("3", "Power Drill", ItemStatus.AVAILABLE)
    )
    
    val borrowedItems = listOf(
        BorrowItem("4", "Ladder", ItemStatus.IN_USE, "Mike"),
        BorrowItem("5", "Vacuum Cleaner", ItemStatus.IN_USE, "Anna")
    )
}

enum class ItemStatus {
    AVAILABLE, LENT, IN_USE;
    
    fun toDisplayString() = when(this) {
        AVAILABLE -> "Available"
        LENT -> "Borrowed"
        IN_USE -> "In use"
    }
}

data class BorrowItem(
    val id: String,
    val name: String,
    val status: ItemStatus,
    val associatedUserName: String? = null
)

@Composable
fun HistoryDialog(
    onDismiss: () -> Unit
) {
    val repository = remember { BorrowRepository() }
    val currentUser = remember { FirebaseAuth.getInstance().currentUser }
    var transactions by remember { androidx.compose.runtime.mutableStateOf<List<HistoryTransaction>>(emptyList()) }
    var isLoading by remember { androidx.compose.runtime.mutableStateOf(true) }

    androidx.compose.runtime.LaunchedEffect(currentUser) {
        val uid = currentUser?.uid ?: return@LaunchedEffect
        
        // Fetch both history (finished) and active (pending/accepted) requests
        val borrowsFinished = repository.getUserPastBorrows(uid)
        val lendsFinished = repository.getUserPastLends(uid)
        val borrowsActive = repository.getUserActiveBorrows(uid)
        val lendsActive = repository.getUserActiveLends(uid)

        val format = SimpleDateFormat("MMM dd", Locale.getDefault())

        val borrowList = (borrowsFinished + borrowsActive).map { req ->
            val dateStr = format.format(req.requestDate.toDate())
            val statusLabel = if (req.status == "pending" || req.status == "accepted") " (Active)" else ""
            HistoryTransaction(req.id, req.itemName + statusLabel, req.ownerName, dateStr, InteractionType.BORROWED)
        }
        val lendList = (lendsFinished + lendsActive).map { req ->
            val dateStr = format.format(req.requestDate.toDate())
            val statusLabel = if (req.status == "pending" || req.status == "accepted") " (Active)" else ""
            HistoryTransaction(req.id, req.itemName + statusLabel, req.requesterName, dateStr, InteractionType.LENT)
        }

        transactions = (borrowList + lendList).sortedByDescending { it.date }
        isLoading = false
    }

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
                        text = "History",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Black
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    color = Color.LightGray,
                    thickness = 1.dp
                )

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (transactions.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No history yet", color = Color.Gray, fontSize = 16.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(transactions) { transaction ->
                            HistoryTransactionCard(transaction = transaction)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryTransactionCard(
    transaction: HistoryTransaction
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (transaction.interactionType == InteractionType.LENT) {
                            Primary.copy(alpha = 0.2f)
                        } else {
                            Color(0xFFE3F2F7)
                        },
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (transaction.interactionType == InteractionType.LENT) "📤" else "📥",
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = transaction.itemName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${transaction.interactionType.toDisplayString()} ${transaction.userName}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Text(
                text = transaction.date,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}
