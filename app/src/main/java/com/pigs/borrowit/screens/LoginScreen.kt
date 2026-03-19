package com.pigs.borrowit.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.pigs.borrowit.control.AuthMode

@Composable
fun LoginScreen(navController: NavController) {
    AuthScreen(
        mode = AuthMode.LOGIN,
        onSwitchMode = { navController.navigate("signup") },
        onSubmit = { username, _, password ->
            // lógica login
        }
    )
}