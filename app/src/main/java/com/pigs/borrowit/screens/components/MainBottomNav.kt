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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.navigation.NavController
import com.pigs.borrowit.R
import com.pigs.borrowit.presentation.navigation.Screen
import com.pigs.borrowit.presentation.navigation.navigateSingleInStack

@Composable
fun MainBottomNav(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val currentRoute = navController.currentBackStackEntry?.destination?.route
    val currentScreen = when (currentRoute) {
        Screen.Home.route -> "Home"
        Screen.Profile.route -> "Profile"
        Screen.Communities.route -> "Communities"
        Screen.Items.route -> "Items"
        else -> ""
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(top = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavItem(
            painterResource(id = R.drawable.home_symbol),
            screen = "Home",
            currentScreen,
            onClick = {
                navController.navigateSingleInStack(Screen.Home.route)
            }
        )
        BottomNavItem(
            painterResource(id = R.drawable.community_symbol),
            screen = "Communities",
            currentScreen,
            onClick = {
                navController.navigateSingleInStack(Screen.Communities.route)
            }
        )
        BottomNavItem(
            rememberVectorPainter(Icons.Default.Inventory),
            screen = "Items",
            currentScreen,
            onClick = {
                navController.navigateSingleInStack(Screen.Items.route)
            }
        )
        BottomNavItem(
            painterResource(id = R.drawable.user_symbol),
            screen = "Profile",
            currentScreen,
            onClick = {
                navController.navigateSingleInStack(Screen.Profile.route)
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