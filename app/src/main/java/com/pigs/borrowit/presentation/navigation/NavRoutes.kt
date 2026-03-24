package com.pigs.borrowit.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val iconFilled: ImageVector  // Versión "rellena" del icono
) {
    data object Home : Screen(
        route = "home",
        title = "Inicio",
        icon = Icons.Outlined.Home,
        iconFilled = Icons.Filled.Home
    )

    data object Search : Screen(
        route = "search",
        title = "Buscar",
        icon = Icons.Outlined.Search,
        iconFilled = Icons.Filled.Search
    )

    data object Items : Screen(
        route = "items",
        title = "Inventario",
        icon = Icons.Outlined.AddCircleOutline,
        iconFilled = Icons.Filled.AddCircleOutline
    )

    data object Profile : Screen(
        route = "profile",
        title = "Perfil",
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
        title = "Registro",
        icon = Icons.Outlined.Person,
        iconFilled = Icons.Filled.Person
    )

    data object CommunityDetail : Screen(
        route = "communityDetail/{name}/{description}/{bannerUrl}/{profileUrl}",
        title = "Comunidad",
        icon = Icons.Outlined.Person,
        iconFilled = Icons.Filled.Person
    )

    companion object {
        val items = listOf(Home, Search, Profile)
    }
}