package com.pigs.borrowit.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.pigs.borrowit.presentation.components.MainBottomNav
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
            //TODO: Agregar pantalla de inicio
            MainBottomNav(navController)
        }
        //TODO: Agregar resto de composables
    }
}