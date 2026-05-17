package com.pigs.borrowit.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.pigs.borrowit.data.model.Item
import com.pigs.borrowit.data.model.CommunityMember
import com.pigs.borrowit.data.repositories.AuthRepository
import com.pigs.borrowit.data.repositories.CommunityRepository
import com.pigs.borrowit.data.repositories.ItemRepository
import com.pigs.borrowit.screens.components.EditCommDialog
import com.pigs.borrowit.screens.components.ItemCard
import com.pigs.borrowit.screens.components.ItemDetailDialog
import com.pigs.borrowit.ui.theme.Primary
import com.pigs.borrowit.utils.ImageUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommScreen(
    navController: NavController,
    communityId: String,
    name: String,
    description: String,
    bannerUrl: String?,
    profileUrl: String?
) {
    val itemRepository = remember { ItemRepository() }
    val communityRepository = remember { CommunityRepository() }
    val authRepository = remember { AuthRepository() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val itemsInCommunityState by itemRepository.getItemsByCommunityFlow(communityId).collectAsState(initial = emptyList())

    val currentUserId = remember { authRepository.getCurrentUserId() ?: "" }

    // Use Uri.decode to handle potential '+' or '%20' from navigation parameters
    val initialName = remember(name) { 
        try { java.net.URLDecoder.decode(name, "UTF-8") } catch (e: Exception) { name.replace("+", " ") }
    }
    val initialDescription = remember(description) { 
        try { java.net.URLDecoder.decode(description, "UTF-8") } catch (e: Exception) { description.replace("+", " ") }
    }

    var currentName by remember { mutableStateOf(initialName) }
    var currentDescription by remember { mutableStateOf(initialDescription) }
    var currentBannerUrl by remember { mutableStateOf(if (bannerUrl == "null") null else bannerUrl) }
    var currentProfileUrl by remember { mutableStateOf(if (profileUrl == "null") null else profileUrl) }

    var showEditDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedItem by remember { mutableStateOf<Item?>(null) }
    var showInviteDialog by remember { mutableStateOf(false) }

    var membersList by remember { mutableStateOf<List<CommunityMember>>(emptyList()) }
    var isLoadingMembers by remember { mutableStateOf(false) }
    var creatorId by remember { mutableStateOf("") }

    val loadData = {
        scope.launch {
            isLoadingMembers = true
            // Load fresh data from repository to avoid URL encoding issues
            val comm = communityRepository.getCommunity(communityId)
            comm?.let {
                currentName = it.name
                currentDescription = it.description
                currentBannerUrl = it.bannerUrl
                currentProfileUrl = it.profileUrl
                creatorId = it.creatorId
            }
            
            membersList = communityRepository.getCommunityMembers(communityId)
            isLoadingMembers = false
        }
    }

    LaunchedEffect(communityId) {
        loadData()
    }

    val userIsAdmin = remember(membersList, currentUserId) {
        membersList.find { it.userId == currentUserId }?.role == "admin"
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
                    if (userIsAdmin) {
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Primary)
                        }
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
                        if (!currentBannerUrl.isNullOrEmpty() && currentBannerUrl != "null") {
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
                            if (!currentProfileUrl.isNullOrEmpty() && currentProfileUrl != "null") {
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
                if (itemsInCommunityState.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No items found in this community", color = Color.Gray)
                        }
                    }
                }
                items(itemsInCommunityState) { item ->
                    ItemCard(item = item, onClick = { selectedItem = item })
                    Spacer(modifier = Modifier.height(12.dp))
                }
            } else {
                item {
                    Button(
                        onClick = { showInviteDialog = true },
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
                
                if (isLoadingMembers) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Primary)
                        }
                    }
                } else {
                    items(membersList) { member ->
                        MemberCard(
                            member = member,
                            isCurrentUser = member.userId == currentUserId,
                            isOwner = member.userId == creatorId,
                            canManage = userIsAdmin && member.userId != currentUserId && member.userId != creatorId,
                            onRemove = { 
                                scope.launch {
                                    communityRepository.removeMemberFromCommunity(communityId, member.userId)
                                    loadData()
                                }
                            },
                            onRoleChange = { newRole ->
                                scope.launch {
                                    communityRepository.updateMemberRole(communityId, member.userId, newRole)
                                    loadData()
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    if (showInviteDialog) {
        val inviteCode = remember(communityId) {
            Base64.encodeToString(communityId.toByteArray(), Base64.NO_WRAP)
        }
        
        AlertDialog(
            onDismissRequest = { showInviteDialog = false },
            title = { Text("Invite Members", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Share this code with the person you want to invite:")
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.LightGray.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color.LightGray)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = inviteCode,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Invite Code", inviteCode)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Code copied to clipboard", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Primary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showInviteDialog = false }) {
                    Text("Close", color = Primary)
                }
            }
        )
    }

    if (showEditDialog) {
        EditCommDialog(
            initialName = currentName,
            initialDescription = currentDescription,
            initialBannerUrl = currentBannerUrl,
            initialProfileUrl = currentProfileUrl,
            onDismiss = { showEditDialog = false },
            onSave = { newName, newDesc, newBanner, newProfile ->
                scope.launch {
                    try {
                        var finalBannerUrl = currentBannerUrl
                        var finalProfileUrl = currentProfileUrl

                        // Upload new banner if changed
                        if (newBanner != currentBannerUrl && newBanner?.startsWith("content://") == true) {
                            val compressed = ImageUtils.compressImage(context, Uri.parse(newBanner))
                            compressed?.let { 
                                finalBannerUrl = communityRepository.uploadImage(it, "community_banners")
                            }
                        } else if (newBanner == null) {
                            finalBannerUrl = null
                        }

                        // Upload new profile if changed
                        if (newProfile != currentProfileUrl && newProfile?.startsWith("content://") == true) {
                            val compressed = ImageUtils.compressImage(context, Uri.parse(newProfile))
                            compressed?.let { 
                                finalProfileUrl = communityRepository.uploadImage(it, "community_profiles")
                            }
                        } else if (newProfile == null) {
                            finalProfileUrl = null
                        }

                        communityRepository.updateCommunity(
                            communityId = communityId,
                            name = newName,
                            description = newDesc,
                            bannerUrl = finalBannerUrl,
                            profileUrl = finalProfileUrl
                        )

                        currentName = newName
                        currentDescription = newDesc
                        currentBannerUrl = finalBannerUrl
                        currentProfileUrl = finalProfileUrl
                    } catch (e: Exception) {
                        // Handle error
                    }
                }
                showEditDialog = false
            },
            onDelete = {
                scope.launch {
                    try {
                        communityRepository.deleteCommunity(communityId)
                        showEditDialog = false
                        navController.popBackStack()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error deleting community", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onLeave = {
                scope.launch {
                    try {
                        communityRepository.removeMemberFromCommunity(communityId, currentUserId)
                        showEditDialog = false
                        navController.popBackStack()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error leaving community", Toast.LENGTH_SHORT).show()
                    }
                }
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
    isCurrentUser: Boolean,
    isOwner: Boolean,
    canManage: Boolean,
    onRemove: () -> Unit,
    onRoleChange: (String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val isAdmin = member.role == "admin"

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
                    .background(if (isAdmin) Primary.copy(alpha = 0.1f) else Color.LightGray.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isAdmin) Icons.Default.Shield else Icons.Default.Person,
                    contentDescription = null,
                    tint = if (isAdmin) Primary else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = member.userName + if (isCurrentUser) " (You)" else "",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isOwner) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.Star, contentDescription = "Owner", tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                        Text("Owner", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFFB300), fontWeight = FontWeight.Bold)
                    }
                }
                Text(
                    text = member.role.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isAdmin) Primary else Color.Gray
                )
            }
            
            if (canManage) {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        if (isAdmin) {
                            DropdownMenuItem(
                                text = { Text("Remove Admin Privileges") },
                                onClick = {
                                    onRoleChange("member")
                                    showMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp)) }
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text("Promote to Admin") },
                                onClick = {
                                    onRoleChange("admin")
                                    showMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(18.dp)) }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Remove from Community", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                onRemove()
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
