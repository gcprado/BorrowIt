package com.pigs.borrowit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.pigs.borrowit.screens.components.LoginScreen
import com.pigs.borrowit.screens.components.SignUpScreen
import com.pigs.borrowit.presentation.navigation.AppNavGraph
import com.pigs.borrowit.presentation.navigation.GraphRoute
import com.pigs.borrowit.presentation.navigation.Screen
import com.pigs.borrowit.screens.ProfileScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LoginSignUpApp()
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
        startDestination = GraphRoute.MAIN
    )
}