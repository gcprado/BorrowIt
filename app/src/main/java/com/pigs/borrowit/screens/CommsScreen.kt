package com.pigs.borrowit.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.pigs.borrowit.screens.components.MainBottomNav
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

data class Community(
    val name: String,
    val description: String,
    val members: Int,
    val bannerUrl: String? = null,
    val profileUrl: String? = null,
    val bannerColor: Color = Color(0xFFE0E0E0)
)

@Composable
fun CommsScreen(navController: NavController) {
    val communities = remember {
        listOf(
            Community(
                name = "Mechanics",
                description = "Everything about car repair, tools sharing and engine maintenance.",
                members = 152,
                bannerUrl = "file:///android_asset/communities/mechanics/BannerGris.jpg",
                profileUrl = "file:///android_asset/communities/mechanics/mechanics.jpg"
            ),
            Community(
                name = "Gardening",
                description = "Share your seeds, tools and tips for a beautiful green garden.",
                members = 89,
                bannerUrl = "file:///android_asset/communities/gardening/BannerVerde.jpg",
                profileUrl = "file:///android_asset/communities/gardening/gardering.jpg"
            ),
            Community(
                name = "Sports",
                description = "Find partners for football, tennis or share your sports equipment.",
                members = 214,
                bannerUrl = "file:///android_asset/communities/sports/BannerAmarillo.jpg",
                profileUrl = "file:///android_asset/communities/sports/sports.jpg"
            ),
            Community(
                name = "IT & Computing",
                description = "Tech support, hardware sharing and software development discussions.",
                members = 342,
                bannerUrl = "file:///android_asset/communities/technology/BannerAzul.jpg",
                profileUrl = "file:///android_asset/communities/technology/computing.jpg"
            ),
            Community(
                name = "Video Games",
                description = "Gaming community. Borrow consoles, trade games and play together.",
                members = 528,
                bannerUrl = "file:///android_asset/communities/videogames/BannerMorado.jpg",
                profileUrl = "file:///android_asset/communities/videogames/videogames.jpg"
            )
        )
    }

    var selectedSort by remember { mutableStateOf("Newest") }

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
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Sort by:", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(end = 8.dp))
                SortButton("Newest", selectedSort == "Newest") { selectedSort = "Newest" }
                Spacer(modifier = Modifier.width(8.dp))
                SortButton("Most Popular", selectedSort == "Most Popular") { selectedSort = "Most Popular" }
                Spacer(modifier = Modifier.width(8.dp))
                SortButton("Alphabetical", selectedSort == "Alphabetical") { selectedSort = "Alphabetical" }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Grid of Communities
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(communities) { community ->
                    CommunityCard(community) {
                        val encodedDescription = URLEncoder.encode(community.description, StandardCharsets.UTF_8.toString())
                        val encodedBannerUrl = community.bannerUrl?.let { URLEncoder.encode(it, StandardCharsets.UTF_8.toString()) } ?: "null"
                        val encodedProfileUrl = community.profileUrl?.let { URLEncoder.encode(it, StandardCharsets.UTF_8.toString()) } ?: "null"
                        
                        navController.navigate("communityDetail/${community.name}/$encodedDescription/$encodedBannerUrl/$encodedProfileUrl")
                    }
                }
            }
        }

        // Navbar
        MainBottomNav(
            navController = navController,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun SortButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF1976D2) else Color.Transparent,
            contentColor = if (isSelected) Color.White else Color.Gray
        ),
        shape = RoundedCornerShape(8.dp),
        border = if (!isSelected) BorderStroke(1.dp, Color.LightGray) else null,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        modifier = Modifier.height(36.dp)
    ) {
        Text(text = text, fontSize = 12.sp)
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
                        .padding(top = 20.dp) // Space for the overlapping profile image
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
