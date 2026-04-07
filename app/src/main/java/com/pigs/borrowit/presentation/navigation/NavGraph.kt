package com.pigs.borrowit.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.pigs.borrowit.screens.CommsScreen
import com.pigs.borrowit.screens.HomeScreen
import com.pigs.borrowit.screens.ItemsScreen
import com.pigs.borrowit.screens.ManageItemsScreen
import com.pigs.borrowit.screens.ProfileScreen
import com.pigs.borrowit.screens.components.LoginScreen
import com.pigs.borrowit.screens.components.SignUpScreen


object GraphRoute {
    const val AUTH = "auth"
    const val MAIN = "main"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = GraphRoute.AUTH
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        authGraph(navController)
        mainGraph(navController)
    }
}

// Separar por features/modulos
fun NavGraphBuilder.authGraph(navController: NavHostController) {
    navigation(
        startDestination = Screen.Login.route,
        route = GraphRoute.AUTH
    ) {
        composable(Screen.Login.route) {
            LoginScreen(navController)
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(navController)
        }
    }
}

fun NavGraphBuilder.mainGraph(navController: NavHostController) {
    navigation(
        startDestination = Screen.Home.route,
        route = GraphRoute.MAIN
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }
        composable(Screen.Search.route) {
            CommsScreen(navController)
        }
        composable(Screen.Items.route) {
            ItemsScreen(navController)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController)
        }
        composable(Screen.ManageItems.route) {
            ManageItemsScreen(navController)
        }
    }
}