package com.pigs.borrowit.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.pigs.borrowit.ui.theme.Primary
import com.pigs.borrowit.ui.theme.PrimaryLight
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import coil.compose.AsyncImage
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
    val decodedDescription = URLDecoder.decode(description, StandardCharsets.UTF_8.toString())
    val decodedBannerUrl = bannerUrl?.let { if (it == "null") null else URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) }
    val decodedProfileUrl = profileUrl?.let { if (it == "null") null else URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) }

    var selectedItem by remember { mutableStateOf<CommunityItem?>(null) }

    // Item generator based on community name with specific dates
    val itemsInCommunity = remember(name) {
        when (name) {
            "Mechanics" -> listOf(
                CommunityItem("m1", "Hydraulic Jack", "Heavy duty 3-ton jack for lifting vehicles.", listOf("file:///android_asset/communities/mechanics/HydraulicJack.jpg"), "Mike Wheeler", "Excellent condition", "2023-11-01", "2023-11-05"),
                CommunityItem("m2", "Torque Wrench", "Digital torque wrench, very precise.", listOf("file:///android_asset/communities/mechanics/TorqueWrench.jpg"), "Dustin Henderson", "Good condition", "2023-11-10", "2023-11-15"),
                CommunityItem("m3", "OBD2 Scanner", "Bluetooth diagnostic tool for all cars.", listOf("file:///android_asset/communities/mechanics/Obd2Scanner.jpg"), "Lucas Sinclair", "New", "2023-11-01", "2023-12-31")
            )
            "Gardening" -> listOf(
                CommunityItem("g1", "Lawn Mower", "Electric mower, very quiet and efficient.", listOf("file:///android_asset/communities/gardening/LawnMower.jpg"), "Nancy Wheeler", "Used", "2023-11-06", "2023-11-10"),
                CommunityItem("g2", "Pruning Shears", "Professional shears for roses and trees.", listOf("file:///android_asset/communities/gardening/PruningShears.jpeg"), "Steve Harrington", "Excellent condition", "2023-11-01", "2023-11-20"),
                CommunityItem("g3", "Wheelbarrow", "Sturdy wheelbarrow for soil and rocks.", listOf("file:///android_asset/communities/gardening/Wheelbarrow.jpg"), "Jonathan Byers", "Acceptable condition", "2023-11-05", "2023-11-15")
            )
            "Sports" -> listOf(
                CommunityItem("s1", "Tennis Racket", "Wilson Pro Staff, newly restrung.", listOf("file:///android_asset/communities/sports/TennisRacket.jpg"), "Jim Hopper", "Good condition", "2023-11-12", "2023-11-14"),
                CommunityItem("s2", "Surfboard", "7'2\" Funboard, perfect for beginners.", listOf("file:///android_asset/communities/sports/Surfboard.jpg"), "Max Mayfield", "Excellent condition", "2023-11-18", "2023-11-19"),
                CommunityItem("s3", "Football Boots", "Nike Mercurial, Size 10 US.", listOf("file:///android_asset/communities/sports/FootballBoots.jpg"), "Will Byers", "New", "2023-11-01", "2023-11-30")
            )
            "IT & Computing" -> listOf(
                CommunityItem("i1", "Mechanical Keyboard", "Cherry MX Blue switches, RGB lighting.", listOf("file:///android_asset/communities/technology/MechanicalKeyboard.jpg"), "Robin Buckley", "Excellent condition", "2023-11-05", "2023-11-15"),
                CommunityItem("i2", "27\" Monitor", "4K resolution, IPS panel, great for coding.", listOf("file:///android_asset/communities/technology/MonitorGaming.jpg"), "Erica Sinclair", "Good condition", "2023-11-06", "2023-11-10"),
                CommunityItem("i3", "Raspberry Pi 4", "8GB RAM version, includes case.", listOf("file:///android_asset/communities/technology/RaspberryPi4.jpg"), "Murray Bauman", "New", "2023-11-01", "2023-12-01")
            )
            "Video Games" -> listOf(
                CommunityItem("v1", "Nintendo Switch", "OLED version with Mario Kart 8.", listOf("file:///android_asset/communities/videogames/NintendoSwitch.jpg"), "Eleven", "Excellent condition", "2023-11-25", "2023-11-26"),
                CommunityItem("v2", "PS5 DualSense", "Midnight Black controller.", listOf("file:///android_asset/communities/videogames/PS5DualSense.jpg"), "Joyce Byers", "Good condition", "2023-11-01", "2023-11-10"),
                CommunityItem("v3", "Zelda: Tears of the Kingdom", "Physical cartridge for Switch.", listOf("file:///android_asset/communities/videogames/ZeldaTearsoftheKingdom.jpg"), "Bob Newby", "New", "2023-11-01", "2023-11-30")
            )
            else -> emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(name) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header Section
            item {
                Column {
                    // Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Color(0xFFE0E0E0))
                    ) {
                        if (decodedBannerUrl != null && decodedBannerUrl.isNotEmpty()) {
                            AsyncImage(
                                model = decodedBannerUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        // Profile Image (Overlapping)
                        Box(
                            modifier = Modifier
                                .offset(y = (-40).dp)
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(4.dp, Color.White, CircleShape)
                        ) {
                            if (decodedProfileUrl != null && decodedProfileUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = decodedProfileUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0xFF333333)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Groups,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                            }
                        }

                        // Community Name and Description
                        Column(
                            modifier = Modifier
                                .padding(top = 50.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = decodedDescription,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.DarkGray
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            OutlinedButton(
                                onClick = { /* Share logic */ },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color.LightGray)
                            ) {
                                Text("Share Community", color = Color.Gray)
                            }
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            Text(
                                text = "Available Items",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                    }
                }
            }

            // List of Items
            items(itemsInCommunity) { item ->
                ItemCard(item) { selectedItem = item }
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Item Detail Dialog
    selectedItem?.let { item ->
        ItemDetailDialog(
            item = item,
            onDismiss = { selectedItem = null }
        )
    }
}

@Composable
fun ItemCard(item: CommunityItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Item Image
            AsyncImage(
                model = item.imageUrls.firstOrNull(),
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Item Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "By ${item.author}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary
                    )
                }
            }
        }
    }
}

@Composable
fun ItemDetailDialog(item: CommunityItem, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(top = 16.dp),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header with Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Image Gallery
                    if (item.imageUrls.size > 1) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(item.imageUrls) { url ->
                                AsyncImage(
                                    model = url,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .width(280.dp)
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    } else {
                        AsyncImage(
                            model = item.imageUrls.firstOrNull(),
                            contentDescription = null,
                            modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Published by ${item.author}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Condition Chip
                    Surface(
                        color = PrimaryLight,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info, 
                                contentDescription = null, 
                                modifier = Modifier.size(16.dp),
                                tint = com.pigs.borrowit.ui.theme.PrimaryDark
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item.condition,
                                style = MaterialTheme.typography.labelMedium,
                                color = com.pigs.borrowit.ui.theme.PrimaryDark,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Description",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Availability
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CalendarToday, 
                            contentDescription = null, 
                            modifier = Modifier.size(20.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Availability Range",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.Gray
                            )
                            Text(
                                text = "${item.startDate} to ${item.endDate}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }

                // Action Button
                Button(
                    onClick = { /* Request logic */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = com.pigs.borrowit.ui.theme.ActionGreen, contentColor = androidx.compose.ui.graphics.Color.White)
                ) {
                    Text("Request Item", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
