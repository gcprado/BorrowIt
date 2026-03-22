package com.pigs.borrowit.screens.components

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.pigs.borrowit.control.AuthMode
import com.pigs.borrowit.presentation.navigation.navigateAndClearStack
import com.pigs.borrowit.screens.AuthScreen

@Composable
fun SignUpScreen(navController: NavController) {
    AuthScreen(
        mode = AuthMode.SIGNUP,
        onSwitchMode = { navController.popBackStack() },
        onSubmit = { username, email, password ->
            // lógica registro
            navController.navigateAndClearStack("main")
            //TODO: Insertar navegación a MAIN
        }
    )
}