package com.pigs.borrowit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pigs.borrowit.presentation.navigation.AppNavGraph
import com.pigs.borrowit.presentation.navigation.GraphRoute
import com.pigs.borrowit.screens.components.LoginScreen
import com.pigs.borrowit.screens.components.SignUpScreen


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            //LoginSignUpApp()
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
fun BorrowitApp(){
    val navController = rememberNavController()
    AppNavGraph(
        navController = navController,
        startDestination = GraphRoute.AUTH
    )
}