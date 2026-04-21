package com.pigs.borrowit.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val iconFilled: ImageVector
) {
    data object Home : Screen(
        route = "home",
        title = "Home",
        icon = Icons.Outlined.Home,
        iconFilled = Icons.Filled.Home
    )

    data object Communities : Screen(
        route = "communities",
        title = "Communities",
        icon = Icons.Outlined.Groups,
        iconFilled = Icons.Filled.Groups
    )

    data object Items : Screen(
        route = "items",
        title = "Inventory",
        icon = Icons.Outlined.AddCircleOutline,
        iconFilled = Icons.Filled.AddCircleOutline
    )

    data object Profile : Screen(
        route = "profile",
        title = "Profile",
        icon = Icons.Outlined.Person,
        iconFilled = Icons.Filled.Person
    )

    data object Login : Screen(
        route = "login",
        title = "Login",
        icon = Icons.Outlined.Person,
        iconFilled = Icons.Filled.Person
    )

    data object SignUp : Screen(
        route = "signup",
        title = "Register",
        icon = Icons.Outlined.Person,
        iconFilled = Icons.Filled.Person
    )

    data object CommunityDetail : Screen(
        // Cambiamos a parámetros de consulta (?) para los campos opcionales
        route = "communityDetail/{id}/{name}/{description}?bannerUrl={bannerUrl}&profileUrl={profileUrl}",
        title = "Community",
        icon = Icons.Outlined.Person,
        iconFilled = Icons.Filled.Person
    )

    companion object {
        val items = listOf(Home, Communities, Profile)
    }
}
