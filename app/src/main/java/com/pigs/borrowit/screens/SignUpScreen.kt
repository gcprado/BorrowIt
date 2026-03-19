package com.pigs.borrowit.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.pigs.borrowit.control.AuthMode

@Composable
fun SignUpScreen(navController: NavController) {
    AuthScreen(
        mode = AuthMode.SIGNUP,
        onSwitchMode = { navController.popBackStack() },
        onSubmit = { username, email, password ->
            // lógica registro
        }
    )
}