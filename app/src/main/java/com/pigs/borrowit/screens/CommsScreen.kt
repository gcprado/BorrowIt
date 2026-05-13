package com.pigs.borrowit.screens

import android.net.Uri
import android.util.Base64
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.Timestamp
import com.pigs.borrowit.data.model.Community
import com.pigs.borrowit.data.repositories.AuthRepository
import com.pigs.borrowit.data.repositories.CommunityRepository
import com.pigs.borrowit.screens.components.CreateCommDialog
import com.pigs.borrowit.screens.components.MainBottomNav
import com.pigs.borrowit.ui.theme.Primary
import com.pigs.borrowit.utils.ImageUtils
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

enum class SortType {
    ALPHABETICAL, CREATION, MODIFICATION
}

@Composable
fun CommsScreen(navController: NavController) {
    val communityRepository = remember { CommunityRepository() }
    val authRepository = remember { AuthRepository() }
    val scope = rememberCoroutineScope()
    
    var communities by remember { mutableStateOf<List<Community>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedSort by remember { mutableStateOf(SortType.MODIFICATION) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showSearchDialog by remember { mutableStateOf(false) }
    val gridState = rememberLazyGridState()

    val context = LocalContext.current

    // Fetch communities from Firestore
    val loadCommunities = {
        scope.launch {
            isLoading = true
            val userId = authRepository.getCurrentUserId()
            if (userId != null) {
                communities = communityRepository.getUserCommunities(userId)
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadCommunities()
    }

    val sortedCommunities = remember(communities, selectedSort) {
        when (selectedSort) {
            SortType.ALPHABETICAL -> communities.sortedBy { it.name.lowercase() }
            SortType.CREATION -> communities.sortedByDescending { it.createdAt }
            SortType.MODIFICATION -> communities.sortedByDescending { it.updatedAt }
        }
    }

    // Scroll to top when sorting changes
    LaunchedEffect(selectedSort) {
        if (communities.isNotEmpty()) {
            gridState.scrollToItem(0)
        }
    }

    Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
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

            if (isLoading) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            } else if (communities.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("You don't have any communities yet.", color = Color.Gray)
                }
            } else {
                // Grid of Communities
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(sortedCommunities, key = { it.id }) { community ->
                        CommunityCard(community) {
                            val encodedId = URLEncoder.encode(community.id, StandardCharsets.UTF_8.toString())
                            val encodedName = URLEncoder.encode(community.name, StandardCharsets.UTF_8.toString())
                            val encodedDescription = URLEncoder.encode(community.description, StandardCharsets.UTF_8.toString())
                            
                            // Navegación con Query Parameters opcionales
                            var route = "communityDetail/$encodedId/$encodedName/$encodedDescription"
                            val params = mutableListOf<String>()
                            if (!community.bannerUrl.isNullOrEmpty()) {
                                params.add("bannerUrl=${URLEncoder.encode(community.bannerUrl, StandardCharsets.UTF_8.toString())}")
                            }
                            if (!community.profileUrl.isNullOrEmpty()) {
                                params.add("profileUrl=${URLEncoder.encode(community.profileUrl, StandardCharsets.UTF_8.toString())}")
                            }
                            
                            if (params.isNotEmpty()) {
                                route += "?" + params.joinToString("&")
                            }
                            
                            navController.navigate(route)
                        }
                    }
                }
            }
        }

        // Action Buttons (Search and Create)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 100.dp, end = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FloatingActionButton(
                onClick = { showSearchDialog = true },
                containerColor = Color.White,
                contentColor = Primary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Search, contentDescription = "Search Community")
            }

            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = Primary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Community")
            }
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
                    val userId = authRepository.getCurrentUserId() ?: return@CreateCommDialog
                    showCreateDialog = false // Cerramos el diálogo primero para liberar la UI

                    scope.launch {
                        try {
                            isLoading = true
                            // Obtenemos el nombre real del usuario antes de crear
                            val username = authRepository.getUsername(userId)

                            // Subida de imágenes a Firebase Storage
                            val bannerUrl = banner?.let { uriString ->
                                val compressed = ImageUtils.compressImage(context, Uri.parse(uriString))
                                compressed?.let { communityRepository.uploadImage(it, "community_banners") }
                            } ?: ""

                            val profileUrl = profile?.let { uriString ->
                                val compressed = ImageUtils.compressImage(context, Uri.parse(uriString))
                                compressed?.let { communityRepository.uploadImage(it, "community_profiles") }
                            } ?: ""

                            val newCommunity = Community(
                                name = name,
                                description = desc,
                                bannerUrl = bannerUrl,
                                profileUrl = profileUrl,
                                creatorId = userId,
                                createdAt = Timestamp.now(),
                                updatedAt = Timestamp.now(),
                                memberCount = 1
                            )

                            // Operación asíncrona suspendida
                            communityRepository.createCommunity(newCommunity, username)

                            // Recarga de datos
                            communities = communityRepository.getUserCommunities(userId)
                            isLoading = false
                        } catch (e: Exception) {
                            isLoading = false
                        }
                    }
                }
            )
        }

        if (showSearchDialog) {
            var inviteCode by remember { mutableStateOf("") }
            var searchError by remember { mutableStateOf<String?>(null) }
            var isSearching by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { showSearchDialog = false },
                title = { Text("Join Community", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Enter the invitation code to find and join a community.")
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = inviteCode,
                            onValueChange = { inviteCode = it; searchError = null },
                            label = { Text("Invitation Code") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = searchError != null,
                            supportingText = { searchError?.let { Text(it) } },
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    isSearching = true
                                    val communityId = String(Base64.decode(inviteCode, Base64.NO_WRAP))
                                    val community = communityRepository.getCommunity(communityId)
                                    
                                    if (community != null) {
                                        val userId = authRepository.getCurrentUserId()
                                        val username = authRepository.getUsername(userId!!)
                                        
                                        communityRepository.addMemberToCommunity(communityId, userId, username)
                                        showSearchDialog = false
                                        loadCommunities() // Refresh list
                                    } else {
                                        searchError = "Community not found. Please check the code."
                                    }
                                } catch (e: Exception) {
                                    searchError = "Invalid code format."
                                } finally {
                                    isSearching = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        enabled = inviteCode.isNotBlank() && !isSearching
                    ) {
                        if (isSearching) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        else Text("Join")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSearchDialog = false }) {
                        Text("Cancel")
                    }
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
        Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            Column {
                // Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(Color(0xFFE0E0E0))
                ) {
                    if (!community.bannerUrl.isNullOrEmpty()) {
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
                        text = "${community.memberCount} members",
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
                if (!community.profileUrl.isNullOrEmpty()) {
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
