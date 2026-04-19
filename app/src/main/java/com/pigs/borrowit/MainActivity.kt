package com.pigs.borrowit

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pigs.borrowit.data.repositories.ItemRepository
import com.pigs.borrowit.presentation.navigation.AppNavGraph
import com.pigs.borrowit.presentation.navigation.GraphRoute
import com.pigs.borrowit.screens.LoginScreen
import com.pigs.borrowit.screens.SignUpScreen


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            BorrowitApp()
        }
    }
}

@Composable
fun LoginSignUpApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController) }
        composable("signup") { SignUpScreen(navController) }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLogin() {
    LoginSignUpApp()
}

@Composable
fun BorrowitApp() {
    val context = LocalContext.current
    val repository = remember { ItemRepository() }
    val prefs = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }

    LaunchedEffect(Unit) {
        if (!prefs.getBoolean("migrated_availability", false)) {
            try {
                repository.migrateAvailabilityField()
                prefs.edit().putBoolean("migrated_availability", true).apply()
            } catch (e: Exception) {
                // Manejar error (opcional)
            }
        }
    }

    val navController = rememberNavController()
    AppNavGraph(
        navController = navController,
        startDestination = GraphRoute.AUTH
    )
}