package com.pigs.borrowit.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.pigs.borrowit.presentation.navigation.Screen
import com.pigs.borrowit.screens.components.CreateCommDialog
import com.pigs.borrowit.screens.components.MainBottomNav
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Date

data class Community(
    val name: String,
    val description: String,
    val members: Int,
    val bannerUrl: String? = null,
    val profileUrl: String? = null,
    val bannerColor: Color = Color(0xFFE0E0E0),
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

enum class SortType {
    ALPHABETICAL, CREATION, MODIFICATION
}

@Composable
fun CommsScreen(navController: NavController) {
    val communities = remember {
        mutableStateListOf(
            Community(
                name = "Mechanics",
                description = "Everything about car repair, tools sharing and engine maintenance.",
                members = 152,
                bannerUrl = "file:///android_asset/communities/mechanics/BannerGris.jpg",
                profileUrl = "file:///android_asset/communities/mechanics/mechanics.jpg",
                createdAt = Date(System.currentTimeMillis() - 10000000),
                updatedAt = Date(System.currentTimeMillis() - 5000000)
            ),
            Community(
                name = "Gardening",
                description = "Share your seeds, tools and tips for a beautiful green garden.",
                members = 89,
                bannerUrl = "file:///android_asset/communities/gardening/BannerVerde.jpg",
                profileUrl = "file:///android_asset/communities/gardening/gardering.jpg",
                createdAt = Date(System.currentTimeMillis() - 20000000),
                updatedAt = Date(System.currentTimeMillis() - 10000000)
            ),
            Community(
                name = "Sports",
                description = "Find partners for football, tennis or share your sports equipment.",
                members = 214,
                bannerUrl = "file:///android_asset/communities/sports/BannerAmarillo.jpg",
                profileUrl = "file:///android_asset/communities/sports/sports.jpg",
                createdAt = Date(System.currentTimeMillis() - 5000000),
                updatedAt = Date(System.currentTimeMillis() - 1000000)
            ),
            Community(
                name = "IT & Computing",
                description = "Tech support, hardware sharing and software development discussions.",
                members = 342,
                bannerUrl = "file:///android_asset/communities/technology/BannerAzul.jpg",
                profileUrl = "file:///android_asset/communities/technology/computing.jpg",
                createdAt = Date(System.currentTimeMillis() - 15000000),
                updatedAt = Date(System.currentTimeMillis() - 2000000)
            ),
            Community(
                name = "Video Games",
                description = "Gaming community. Borrow consoles, trade games and play together.",
                members = 528,
                bannerUrl = "file:///android_asset/communities/videogames/BannerMorado.jpg",
                profileUrl = "file:///android_asset/communities/videogames/videogames.jpg",
                createdAt = Date(System.currentTimeMillis() - 30000000),
                updatedAt = Date(System.currentTimeMillis() - 8000000)
            )
        )
    }

    var selectedSort by remember { mutableStateOf(SortType.ALPHABETICAL) }
    var showCreateDialog by remember { mutableStateOf(false) }
    val gridState = rememberLazyGridState()

    val sortedCommunities = remember(communities.size, selectedSort) {
        when (selectedSort) {
            SortType.ALPHABETICAL -> communities.sortedBy { it.name.lowercase() }
            SortType.CREATION -> communities.sortedByDescending { it.createdAt }
            SortType.MODIFICATION -> communities.sortedByDescending { it.updatedAt }
        }
    }

    // Scroll to top when sorting changes
    LaunchedEffect(selectedSort) {
        gridState.scrollToItem(0)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Header
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "My Communities",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                )
                Text(
                    text = "Manage and explore your active communities",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sort Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Sort by:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                SortButton("Name", selectedSort == SortType.ALPHABETICAL) { selectedSort = SortType.ALPHABETICAL }
                SortButton("Creation", selectedSort == SortType.CREATION) { selectedSort = SortType.CREATION }
                SortButton("Activity", selectedSort == SortType.MODIFICATION) { selectedSort = SortType.MODIFICATION }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Grid of Communities
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(sortedCommunities, key = { it.name }) { community ->
                    CommunityCard(community) {
                        val encodedDescription = URLEncoder.encode(community.description, StandardCharsets.UTF_8.toString())
                        val encodedBannerUrl = community.bannerUrl?.let { URLEncoder.encode(it, StandardCharsets.UTF_8.toString()) } ?: "null"
                        val encodedProfileUrl = community.profileUrl?.let { URLEncoder.encode(it, StandardCharsets.UTF_8.toString()) } ?: "null"
                        
                        navController.navigate("communityDetail/${community.name}/$encodedDescription/$encodedBannerUrl/$encodedProfileUrl")
                    }
                }
            }
        }

        // FAB to create community
        FloatingActionButton(
            onClick = { showCreateDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 100.dp, end = 24.dp),
            containerColor = Primary,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "Create Community")
        }

        // Navbar
        MainBottomNav(
            navController = navController,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
        
        if (showCreateDialog) {
            CreateCommDialog(
                onDismiss = { showCreateDialog = false },
                onCreate = { name, desc, banner, profile ->
                    communities.add(Community(
                        name = name,
                        description = desc,
                        members = 1,
                        bannerUrl = banner,
                        profileUrl = profile,
                        createdAt = Date(),
                        updatedAt = Date()
                    ))
                    showCreateDialog = false
                }
            )
        }
    }
}

@Composable
fun SortButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (isSelected) Primary else Color.Transparent,
        contentColor = if (isSelected) Color.White else Color.Gray,
        shape = RoundedCornerShape(12.dp),
        border = if (!isSelected) BorderStroke(1.dp, Color.LightGray) else null,
        modifier = Modifier.height(32.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = text, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
        }
    }
}

@Composable
fun CommunityCard(community: Community, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column {
                // Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(community.bannerColor)
                ) {
                    if (community.bannerUrl != null) {
                        AsyncImage(
                            model = community.bannerUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                
                // Content area
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .padding(top = 20.dp)
                ) {
                    Text(
                        text = community.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = community.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${community.members} members",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }

            // Overlapping Profile Image
            Box(
                modifier = Modifier
                    .padding(start = 12.dp, top = 50.dp)
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, Color.White, CircleShape)
            ) {
                if (community.profileUrl != null) {
                    AsyncImage(
                        model = community.profileUrl,
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
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }
        }
    }
}
