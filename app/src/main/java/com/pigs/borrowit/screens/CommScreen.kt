package com.pigs.borrowit.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.pigs.borrowit.ui.theme.Primary
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.pigs.borrowit.screens.components.EditCommDialog
import com.pigs.borrowit.screens.components.ItemDetailDialog
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

data class CommunityItem(
    val id: String,
    val name: String,
    val description: String,
    val imageUrls: List<String>,
    val author: String,
    val condition: String,
    val startDate: String,
    val endDate: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommScreen(
    navController: NavController,
    name: String,
    description: String,
    bannerUrl: String?,
    profileUrl: String?
) {
    var currentName by remember { mutableStateOf(name) }
    var currentDescription by remember { mutableStateOf(URLDecoder.decode(description, StandardCharsets.UTF_8.toString())) }
    var currentBannerUrl by remember { mutableStateOf(bannerUrl?.let { if (it == "null") null else URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) }) }
    var currentProfileUrl by remember { mutableStateOf(profileUrl?.let { if (it == "null") null else URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) }) }

    var selectedItem by remember { mutableStateOf<CommunityItem?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    val itemsInCommunity = remember(currentName) {
        when (currentName) {
            "Mechanics" -> listOf(
                CommunityItem("m1", "Hydraulic Jack", "Heavy duty 3-ton jack for lifting vehicles.", listOf("file:///android_asset/communities/mechanics/HydraulicJack.jpg"), "Mike Wheeler", "Excellent condition", "2023-11-01", "2023-11-05"),
                CommunityItem("m2", "Torque Wrench", "Digital torque wrench, very precise.", listOf("file:///android_asset/communities/mechanics/TorqueWrench.jpg"), "Dustin Henderson", "Good condition", "2023-11-10", "2023-11-15"),
                CommunityItem("m3", "Screwdriver Set", "Set of 20 screwdrivers for all types of screws.", listOf("file:///android_asset/communities/mechanics/ScrewdriverSet.jpg"), "Lucas Sinclair", "New", "2023-11-12", "2023-11-20")
            )
            "Gardening" -> listOf(
                CommunityItem("g1", "Lawn Mower", "Electric mower, very quiet and efficient.", listOf("file:///android_asset/communities/gardening/LawnMower.jpg"), "Nancy Wheeler", "Used", "2023-11-06", "2023-11-10"),
                CommunityItem("g2", "Pruning Shears", "Sharp shears for bushes and small branches.", listOf("file:///android_asset/communities/gardening/PruningShears.jpg"), "Steve Harrington", "Excellent condition", "2023-11-15", "2023-11-18"),
                CommunityItem("g3", "Garden Rake", "Classic rake for leaves and soil preparation.", listOf("file:///android_asset/communities/gardening/GardenRake.jpg"), "Jonathan Byers", "Good condition", "2023-11-20", "2023-11-25")
            )
            "Sports" -> listOf(
                CommunityItem("s1", "Tennis Racket", "Professional Wilson racket.", listOf("file:///android_asset/communities/sports/TennisRacket.jpg"), "Robin Buckley", "Excellent condition", "2023-11-05", "2023-11-08"),
                CommunityItem("s2", "Basketball", "Official size indoor/outdoor ball.", listOf("file:///android_asset/communities/sports/Basketball.jpg"), "Billy Hargrove", "Used", "2023-11-10", "2023-11-12")
            )
            "IT & Computing" -> listOf(
                CommunityItem("it1", "Mechanical Keyboard", "RGB, Blue switches.", listOf("file:///android_asset/communities/technology/MechanicalKeyboard.jpg"), "Erica Sinclair", "New", "2023-11-20", "2023-11-22"),
                CommunityItem("it2", "External Hard Drive", "1TB SSD, very fast.", listOf("file:///android_asset/communities/technology/HardDrive.jpg"), "Murray Bauman", "Good condition", "2023-11-25", "2023-11-28")
            )
            "Video Games" -> listOf(
                CommunityItem("vg1", "Nintendo Switch", "Console with 2 Joy-Cons.", listOf("file:///android_asset/communities/videogames/Switch.jpg"), "Will Byers", "Excellent condition", "2023-12-01", "2023-12-05"),
                CommunityItem("vg2", "PS5 Controller", "DualSense controller, white.", listOf("file:///android_asset/communities/videogames/PS5Controller.jpg"), "Max Mayfield", "New", "2023-12-10", "2023-12-12")
            )
            else -> emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentName) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Color(0xFFE0E0E0))
                    ) {
                        if (currentBannerUrl != null && currentBannerUrl!!.isNotEmpty()) {
                            AsyncImage(
                                model = currentBannerUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                        Box(
                            modifier = Modifier
                                .offset(y = (-40).dp)
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(4.dp, Color.White, CircleShape)
                        ) {
                            if (currentProfileUrl != null && currentProfileUrl!!.isNotEmpty()) {
                                AsyncImage(
                                    model = currentProfileUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(modifier = Modifier.fillMaxSize().background(Color(0xFF333333)), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Groups, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                                }
                            }
                        }

                        Column(modifier = Modifier.padding(top = 50.dp).fillMaxWidth()) {
                            Text(text = currentName, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = currentDescription, style = MaterialTheme.typography.bodyLarge, color = Color.DarkGray)
                            Spacer(modifier = Modifier.height(24.dp))
                            OutlinedButton(onClick = { }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Color.LightGray)) {
                                Text("Share Community", color = Color.Gray)
                            }
                            Spacer(modifier = Modifier.height(32.dp))
                            Text(text = "Available Items", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(bottom = 16.dp))
                        }
                    }
                }
            }

            items(itemsInCommunity) { item ->
                ItemCard(item) { selectedItem = item }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    if (showEditDialog) {
        EditCommDialog(
            initialName = currentName,
            initialDescription = currentDescription,
            initialBannerUrl = currentBannerUrl,
            initialProfileUrl = currentProfileUrl,
            onDismiss = { showEditDialog = false },
            onSave = { newName, newDesc, newBanner, newProfile ->
                currentName = newName
                currentDescription = newDesc
                currentBannerUrl = newBanner
                currentProfileUrl = newProfile
                showEditDialog = false
            },
            onDelete = {
                showEditDialog = false
                navController.popBackStack()
            }
        )
    }

    selectedItem?.let { item ->
        ItemDetailDialog(
            item = item,
            onDismiss = { selectedItem = null },
            onBorrow = { /* Logic for borrowing if needed */ }
        )
    }
}

@Composable
fun ItemCard(item: CommunityItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(model = item.imageUrls.firstOrNull(), contentDescription = null, modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = item.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(Color.LightGray))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "By ${item.author}", style = MaterialTheme.typography.labelSmall, color = Primary)
                }
            }
        }
    }
}
