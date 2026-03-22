package com.pigs.borrowit.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.pigs.borrowit.R
import com.pigs.borrowit.screens.components.MainBottomNav

// Data class para objetos recomendados
data class RecommendedItem(
    val id: Int,
    val name: String,
    val location: String,
    val rating: Double,
    val borrowersCount: Int,
    val iconRes: Int? = null,
    val iconEmoji: String = "🔧"
)

// Data class para anuncios patrocinados
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
    // Datos de ejemplo - Fáciles de modificar y extender
    val recommendedItems = listOf(
        RecommendedItem(
            id = 1,
            name = "Power Drill Set",
            location = "Available nearby • John's Tools",
            rating = 4.8,
            borrowersCount = 15,
            iconEmoji = "🔧"
        )
    )

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

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = 16.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 80.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header con perfil y saludo
            ProfileHeader()

            // Sección de recomendaciones
            SectionHeader(
                title = "Recommended for you",
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp)
            )

            // Lista de objetos recomendados
            recommendedItems.forEach { item ->
                RecommendedItemCard(item = item)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Sección de anuncios patrocinados
            SectionHeader(
                title = "Sponsored",
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                fontSize = 14,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )

            // Lista de anuncios
            sponsoredAds.forEach { ad ->
                SponsoredAdCard(ad = ad)
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.weight(1f))
        }

        MainBottomNav(navController)
    }
}

@Composable
fun ProfileHeader(
    onNotificationClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 16.dp)
            .drawBehind {
                val lineY = size.height + 8.dp.toPx()
                drawLine(
                    color = Color.Gray,
                    start = Offset(0f, lineY),
                    end = Offset(size.width, lineY),
                    strokeWidth = 0.5.dp.toPx()
                )
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Parte izquierda: imagen de perfil y texto
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.profilepicture_default),
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.LightGray, CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

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

        // Parte derecha: ícono de notificación
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Transparent)
                .clickable { onNotificationClick() },
            contentAlignment = Alignment.Center
        ) {
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
    fontSize: Int = 18,
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
fun RecommendedItemCard(
    item: RecommendedItem,
    onBorrowClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono del objeto
            if (item.iconRes != null) {
                Image(
                    painter = painterResource(id = item.iconRes),
                    contentDescription = item.name,
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.iconEmoji,
                        fontSize = 32.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Información del objeto
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = item.location,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "⭐ ${item.rating} • ${item.borrowersCount} people borrowing",
                    fontSize = 11.sp,
                    color = Color(0xFF4CAF50)
                )
            }

            // Botón de acción
            Box(
                modifier = Modifier
                    .background(Color(0xFF2196F3), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Text(
                    text = "Borrow",
                    fontSize = 12.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun SponsoredAdCard(
    ad: SponsoredAd,
    onCtaClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Imagen del anuncio
            Image(
                painter = painterResource(id = ad.imageRes),
                contentDescription = ad.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                contentScale = ContentScale.Crop
            )

            // Información del anuncio
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = ad.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "by ${ad.brand} • Sponsored",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = ad.description,
                        fontSize = 11.sp,
                        color = Color(0xFF757575)
                    )
                }

                // Botón CTA
                Box(
                    modifier = Modifier
                        .background(ad.ctaColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = ad.ctaText,
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Badge de "Ad"
            Box(
                modifier = Modifier
                    .background(Color(0xFFE0E0E0), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .align(Alignment.End)
                    .padding(bottom = 4.dp, end = 8.dp)
            ) {
                Text(
                    text = "AD",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}