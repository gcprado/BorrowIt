package com.pigs.borrowit.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import com.pigs.borrowit.data.repositories.BorrowRepository
import com.pigs.borrowit.data.repositories.ItemRepository
import com.pigs.borrowit.data.repositories.AuthRepository
import com.pigs.borrowit.screens.components.BorrowItem
import com.pigs.borrowit.screens.components.ItemsTab
import com.pigs.borrowit.screens.components.ItemStatus
import com.pigs.borrowit.ui.theme.Background
import com.pigs.borrowit.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageItemsScreen(
    navController: NavController
) {
    var selectedTab by remember { mutableStateOf(ItemsTab.MY_ITEMS) }

    val borrowRepo = remember { BorrowRepository() }
    val itemRepo = remember { ItemRepository() }
    val currentUser = remember { FirebaseAuth.getInstance().currentUser }

    var myItemsList by remember { mutableStateOf<List<BorrowItem>>(emptyList()) }
    var borrowedItemsList by remember { mutableStateOf<List<BorrowItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(currentUser) {
        val uid = currentUser?.uid ?: return@LaunchedEffect

        combine(
            itemRepo.getItemsByOwnerFlow(uid),
            itemRepo.getItemsByCurrentUserFlow(uid),
            borrowRepo.getActiveBorrowsFlow(uid),
            borrowRepo.getActiveLendsFlow(uid)
        ) { userItems, heldItems, incomingBorrows, outgoingBorrows ->

            // Map My Items
            val mappedMyItems = userItems.map { item ->
                val activeLend = outgoingBorrows
                    .filter { it.itemId == item.id }
                    .maxByOrNull { it.requestDate }

                val borrowerNameOrId = if (activeLend?.requesterName?.isNotBlank() == true) {
                    activeLend.requesterName
                } else if (item.currentUser.isNotBlank() && item.currentUser != item.owner) {
                    item.currentUser // Provide the raw UI to be resolved via UI
                } else null

                BorrowItem(
                    id = item.id,
                    name = item.name,
                    status = if (item.status == "LENT" || activeLend != null || (item.currentUser.isNotBlank() && item.currentUser != item.owner)) ItemStatus.LENT else ItemStatus.AVAILABLE,
                    associatedUserName = borrowerNameOrId
                )
            }

            // Map Borrowed Items (items not owned by you but currently held by you based on Item.currentUser)
            val mappedBorrowedItems = heldItems.map { item ->
                val associatedRequest = incomingBorrows.find { it.itemId == item.id }

                val ownerNameOrId = if (associatedRequest?.ownerName?.isNotBlank() == true) {
                    associatedRequest.ownerName
                } else if (item.owner.isNotBlank()) {
                    item.owner // Provide the raw UI to be resolved via UI
                } else null

                BorrowItem(
                    id = item.id,
                    name = item.name,
                    status = ItemStatus.IN_USE,
                    associatedUserName = ownerNameOrId
                )
            }

            Pair(mappedMyItems, mappedBorrowedItems)
        }.collectLatest { pairResults ->
            myItemsList = pairResults.first
            borrowedItemsList = pairResults.second
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = "Manage Items",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background,
                    titleContentColor = Color.Black
                )
            )

            TabSelector(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            val items = if (selectedTab == ItemsTab.MY_ITEMS) {
                myItemsList
            } else {
                borrowedItemsList
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (items.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No items found", color = Color.Gray, fontSize = 16.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items) { item ->
                        ManageItemCard(
                            item = item,
                            onClick = { /* Handle item click if needed */ }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TabSelector(
    selectedTab: ItemsTab,
    onTabSelected: (ItemsTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TabButton(
            text = "My Items",
            isSelected = selectedTab == ItemsTab.MY_ITEMS,
            onClick = { onTabSelected(ItemsTab.MY_ITEMS) },
            modifier = Modifier.weight(1f)
        )
        TabButton(
            text = "Borrowed Items",
            isSelected = selectedTab == ItemsTab.BORROWED_ITEMS,
            onClick = { onTabSelected(ItemsTab.BORROWED_ITEMS) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = if (isSelected) Primary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) Color.Black else Color.Gray
        )
    }
}

@Composable
fun ManageItemCard(
    item: BorrowItem,
    onClick: () -> Unit
) {
    var resolvedName by remember(item.associatedUserName) {
        mutableStateOf(item.associatedUserName ?: "")
    }

    LaunchedEffect(item.associatedUserName) {
        val nameOrId = item.associatedUserName
        if (nameOrId != null && nameOrId.length > 20 && !nameOrId.contains(" ")) {
            // It looks like a Firebase UID, let's fetch the actual name safely off the main flow
            try {
                val repo = com.pigs.borrowit.data.repositories.AuthRepository()
                resolvedName = repo.getUsername(nameOrId)
            } catch (e: Exception) {
                resolvedName = "Unknown"
            }
        } else if (nameOrId != null) {
            resolvedName = nameOrId
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
                    .size(60.dp)
                    .background(
                        color = Primary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (item.name.isNotEmpty()) item.name.first().toString().uppercase() else "?",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))

                val subtitle = when(item.status) {
                    ItemStatus.LENT -> if (resolvedName.isNotBlank()) "Lent to: $resolvedName" else "Borrowed"
                    ItemStatus.IN_USE -> if (resolvedName.isNotBlank()) "From: $resolvedName" else "In use"
                    else -> item.status.toDisplayString()
                }

                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            StatusBadge(status = item.status.toDisplayString())
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val backgroundColor = when (status) {
        "Available" -> Color(0xFF4CAF50).copy(alpha = 0.15f)
        "Borrowed" -> Primary.copy(alpha = 0.2f)
        "In use" -> Color(0xFFFF9800).copy(alpha = 0.15f)
        else -> Color.LightGray.copy(alpha = 0.2f)
    }

    val textColor = when (status) {
        "Available" -> Color(0xFF2E7D32)
        "Borrowed" -> Color(0xFF1976D2)
        "In use" -> Color(0xFFE65100)
        else -> Color.Gray
    }

    Box(
        modifier = Modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = status,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}
