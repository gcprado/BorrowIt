package com.pigs.borrowit.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.pigs.borrowit.screens.components.EditCommDialog
import com.pigs.borrowit.screens.components.ItemDetailDialog
import com.pigs.borrowit.ui.theme.Primary
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

data class CommunityMember(
    val id: String,
    val name: String,
    val role: String, // "Admin" or "Member"
    val profileUrl: String? = null
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
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Items, 1: Members

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

    val membersList = remember {
        mutableStateListOf(
            CommunityMember("u1", "John Doe (You)", "Admin"),
            CommunityMember("u2", "Mike Wheeler", "Member"),
            CommunityMember("u3", "Dustin Henderson", "Member"),
            CommunityMember("u4", "Lucas Sinclair", "Member"),
            CommunityMember("u5", "Nancy Wheeler", "Member")
        )
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
                            
                            TabRow(
                                selectedTabIndex = selectedTab,
                                containerColor = Color.Transparent,
                                contentColor = Primary,
                                indicator = { tabPositions ->
                                    TabRowDefaults.SecondaryIndicator(
                                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                        color = Primary
                                    )
                                },
                                divider = {}
                            ) {
                                Tab(
                                    selected = selectedTab == 0,
                                    onClick = { selectedTab = 0 },
                                    text = { Text("Items", fontWeight = if(selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
                                )
                                Tab(
                                    selected = selectedTab == 1,
                                    onClick = { selectedTab = 1 },
                                    text = { Text("Members", fontWeight = if(selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }

            if (selectedTab == 0) {
                items(itemsInCommunity) { item ->
                    ItemCard(item) { selectedItem = item }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            } else {
                item {
                    Button(
                        onClick = { /* Invitation logic */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Invite Members", fontWeight = FontWeight.Bold)
                    }
                }
                items(membersList) { member ->
                    MemberCard(
                        member = member,
                        onDelete = { membersList.remove(member) },
                        onPromote = { 
                            val index = membersList.indexOf(member)
                            if (index != -1) {
                                membersList[index] = member.copy(role = "Admin")
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
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
            },
            onLeave = {
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
fun MemberCard(
    member: CommunityMember,
    onDelete: () -> Unit,
    onPromote: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (member.role == "Admin") Primary.copy(alpha = 0.1f) else Color.LightGray.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (member.role == "Admin") Icons.Default.Shield else Icons.Default.Person,
                    contentDescription = null,
                    tint = if (member.role == "Admin") Primary else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = member.role,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (member.role == "Admin") Primary else Color.Gray
                )
            }
            
            // Don't show menu for the current user
            if (!member.name.contains("(You)")) {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        if (member.role != "Admin") {
                            DropdownMenuItem(
                                text = { Text("Promote to Admin") },
                                onClick = {
                                    onPromote()
                                    showMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(18.dp)) }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Remove from Community", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                onDelete()
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error) }
                        )
                    }
                }
            }
        }
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
