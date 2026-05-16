package com.pigs.borrowit.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.pigs.borrowit.R
import com.pigs.borrowit.control.HomeViewModel
import com.pigs.borrowit.data.model.BorrowRequest
import com.pigs.borrowit.data.model.Item
import com.pigs.borrowit.screens.components.ItemDetailDialog
import com.pigs.borrowit.screens.components.MainBottomNav
import com.pigs.borrowit.screens.components.NotificationsDialog
import com.pigs.borrowit.ui.theme.Background
import com.pigs.borrowit.ui.theme.CardBackground
import com.pigs.borrowit.ui.theme.Primary

// Data class for sponsored ads
data class SponsoredAd(
    val id: Int,
    val title: String,
    val brand: String,
    val description: String,
    val imageRes: Int,
    val ctaText: String = "Shop Now",
    val ctaColor: Color = Color(0xFFFF6B6B)
)

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel()
) {
    val userState by viewModel.userState.collectAsState()
    val showNotifications by viewModel.showNotifications.collectAsState()
    val pendingRequests by viewModel.pendingRequests.collectAsState()
    val recommendedItem by viewModel.recommendedItem.collectAsState()
    val sponsoredAds by viewModel.sponsoredAds.collectAsState()

    var selectedItem by remember { mutableStateOf<Item?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(Background).statusBarsPadding()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
        ) {
            // Profile Header
            item {
                ProfileHeader(
                    username = userState?.username ?: "User",
                    profilePicUrl = userState?.profilePicture ?: "",
                    notificationCount = pendingRequests.size,
                    onNotificationClick = { viewModel.setShowNotifications(true) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Recommendations Section
            recommendedItem?.let { item ->
                item {
                    SectionHeader(
                        title = "Recommended for you",
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 8.dp)
                    )
                }

                item {
                    HomeRecommendedCard(
                        item = item,
                        onClick = { selectedItem = item }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Sponsored Section
            if (sponsoredAds.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Sponsored deals",
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 12.dp),
                        fontSize = 14,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                }

                // Ads List
                items(sponsoredAds) { ad ->
                    HomeSponsoredAdCard(ad = ad)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        MainBottomNav(navController, modifier = Modifier.align(Alignment.BottomCenter))

        selectedItem?.let { item ->
            ItemDetailDialog(
                item = item,
                onDismiss = { selectedItem = null },
                onBorrow = { viewModel.borrowItem(item) }
            )
        }

        if (showNotifications) {
            NotificationsDialog(
                requests = pendingRequests,
                onDismiss = { viewModel.setShowNotifications(false) },
                onAccept = { request: BorrowRequest ->
                    viewModel.acceptRequest(request)
                },
                onDecline = { request: BorrowRequest ->
                    viewModel.declineRequest(request)
                }
            )
        }
    }
}

@Composable
fun ProfileHeader(
    username: String,
    profilePicUrl: String,
    notificationCount: Int,
    onNotificationClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 20.dp)
            .drawBehind {
                val lineY = size.height + 10.dp.toPx()
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.5f),
                    start = Offset(0f, lineY),
                    end = Offset(size.width, lineY),
                    strokeWidth = 1.dp.toPx()
                )
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val painter = if (profilePicUrl.isNotEmpty()) {
                rememberAsyncImagePainter(profilePicUrl)
            } else {
                painterResource(id = R.drawable.profilepicture_default)
            }

            Image(
                painter = painter,
                contentDescription = "Profile",
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .border(2.dp, Primary.copy(alpha = 0.5f), CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "Welcome back,",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = username,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }

        Box(modifier = Modifier.wrapContentSize()) {
            IconButton(onClick = onNotificationClick) {
                Icon(
                    painter = painterResource(id = R.drawable.notificacion),
                    contentDescription = "Notifications",
                    modifier = Modifier.size(32.dp),
                    tint = Color.Unspecified
                )
            }
            if (notificationCount > 0) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = 8.dp, y = (-8).dp)
                        .size(18.dp),
                    shape = CircleShape,
                    color = Color.Red,
                    border = BorderStroke(1.5.dp, Color.White)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (notificationCount > 9) "9+" else notificationCount.toString(),
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    fontSize: Int = 20,
    fontWeight: FontWeight = FontWeight.Bold,
    color: Color = Color.Black
) {
    Text(
        text = title,
        modifier = modifier.fillMaxWidth(),
        fontSize = fontSize.sp,
        fontWeight = fontWeight,
        color = color
    )
}

@Composable
fun HomeRecommendedCard(
    item: Item,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                AsyncImage(
                    model = item.picture,
                    contentDescription = item.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    modifier = Modifier.padding(12.dp).align(Alignment.TopEnd),
                    color = Primary,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "FOR YOU",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
                    )
                }
            }
            
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Free",
                        color = Primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Lent by ${item.owner}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Details", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun HomeSponsoredAdCard(ad: SponsoredAd) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = ad.imageRes),
                contentDescription = ad.title,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ad.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = ad.brand,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = ad.ctaColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.clickable { }
                ) {
                    Text(
                        text = ad.ctaText,
                        color = ad.ctaColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 6.dp)
                    )
                }
            }
        }
    }
}
