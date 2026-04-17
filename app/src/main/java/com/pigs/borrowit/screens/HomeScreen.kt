package com.pigs.borrowit.screens

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
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.pigs.borrowit.R
import com.pigs.borrowit.screens.components.ItemDetailDialog
import com.pigs.borrowit.screens.components.MainBottomNav
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
    navController: NavController
) {
    // Recommended item from a community
    val recommendedItem = remember {
        CommunityItem(
            id = "vg1",
            name = "Nintendo Switch",
            description = "Console with 2 Joy-Cons. Perfect for parties or a weekend of gaming.",
            imageUrls = listOf("file:///android_asset/communities/videogames/Switch.jpg"),
            author = "Will Byers",
            condition = "Excellent condition",
            startDate = "2023-12-01",
            endDate = "2023-12-05"
        )
    }

    val sponsoredAds = listOf(
        SponsoredAd(
            id = 1,
            title = "Professional Hair Dryer",
            brand = "Dyson",
            description = "Fast drying • Ionic technology",
            imageRes = R.drawable.hairdryer,
            ctaText = "Shop Now",
            ctaColor = Color(0xFFFF6B6B)
        ),
        SponsoredAd(
            id = 2,
            title = "Electric Drill Pro",
            brand = "Bosch",
            description = "Cordless • 20V Max • 2 batteries included",
            imageRes = R.drawable.electricdrill,
            ctaText = "View Deal",
            ctaColor = Color(0xFF4CAF50)
        )
    )

    var selectedItem by remember { mutableStateOf<CommunityItem?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
        ) {
            // Profile Header
            item {
                ProfileHeader()
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Recommendations Section
            item {
                SectionHeader(
                    title = "Recommended for you",
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 8.dp)
                )
            }

            item {
                HomeRecommendedCard(
                    item = recommendedItem,
                    onClick = { selectedItem = recommendedItem }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Sponsored Section
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

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        MainBottomNav(navController, modifier = Modifier.align(Alignment.BottomCenter))

        selectedItem?.let { item ->
            ItemDetailDialog(
                item = item,
                onDismiss = { selectedItem = null },
                onBorrow = { /* Handle borrow logic */ }
            )
        }
    }
}

@Composable
fun ProfileHeader() {
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
            Image(
                painter = painterResource(id = R.drawable.profilepicture_default),
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
                    text = "John Doe",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }

        IconButton(onClick = { }) {
            Icon(
                painter = painterResource(id = R.drawable.notification_symbol),
                contentDescription = "Notifications",
                modifier = Modifier.size(24.dp),
                tint = Color.Unspecified
            )
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
    item: CommunityItem,
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
                    model = item.imageUrls.firstOrNull(),
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
                        text = "NEW",
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
                    text = "Lent by ${item.author} • Video Games",
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
