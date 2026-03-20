package com.pigs.borrowit.screens.components

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.pigs.borrowit.control.AuthMode
import com.pigs.borrowit.screens.AuthScreen

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