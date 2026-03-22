package com.pigs.borrowit.screens.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pigs.borrowit.R
import com.pigs.borrowit.presentation.navigation.Screen
import com.pigs.borrowit.presentation.navigation.navigateSingleInStack

@Composable
fun MainBottomNav(
    navController: NavController,
    modifier: Modifier = Modifier  // ← Añadido para permitir personalización desde fuera
) {
    val currentRoute = navController.currentBackStackEntry?.destination?.route
    val currentScreen = when (currentRoute) {
        Screen.Home.route -> "Home"
        Screen.Profile.route -> "Profile"
        Screen.Search.route -> "Communities"
        Screen.Items.route -> "Items"
        else -> ""
    }

    // Eliminado el Box innecesario
    Row(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(top = 20.dp)
            /*
            .drawBehind {
                // Línea en la parte superior del navbar
                drawLine(
                    color = Color.Gray,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 0.5.dp.toPx()
                )
            }*/,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavItem(
            painterResource(id = R.drawable.home_symbol),
            screen = "Home",
            currentScreen,
            onClick = {
                navController.navigateSingleInStack("home")
            }
        )
        BottomNavItem(
            painterResource(id = R.drawable.community_symbol),
            screen = "Communities",
            currentScreen,
            onClick = {
                navController.navigateSingleInStack("search")
            }
        )
        BottomNavItem(
            painterResource(id = R.drawable.add_symbol),
            screen = "Items",
            currentScreen,
            onClick = {
                navController.navigateSingleInStack("items")
            }
        )
        BottomNavItem(
            painterResource(id = R.drawable.user_symbol),
            screen = "Profile",
            currentScreen,
            onClick = {
                navController.navigateSingleInStack("profile")
            }
        )
    }
}

@Composable
fun BottomNavItem(
    painter: Painter,
    screen: String?,
    currentScreen: String,
    onClick: () -> Unit,
) {
    val isSelected = screen == currentScreen
    val color = if (isSelected) Color.Black else Color.Gray

    Icon(
        painter = painter,
        contentDescription = screen,
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .size(40.dp)
            .clickable(onClick = onClick),
        tint = color
    )
}