package com.pigs.borrowit.screens

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.pigs.borrowit.control.AuthMode
import com.pigs.borrowit.data.repositories.AuthRepository
import com.pigs.borrowit.presentation.navigation.navigateAndClearStack

@Composable
fun SignUpScreen(navController: NavController) {

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showErrorDialog by remember { mutableStateOf(false) }

    val repo = AuthRepository()

    AuthScreen(
        mode = AuthMode.SIGNUP,
        onSwitchMode = { navController.popBackStack() },
        onSubmit = { username, email, password ->

            // 🔴 Validaciones básicas
            if (username.isBlank() || email.isBlank() || password.isBlank()) {
                errorMessage = "All fields are required"
                showErrorDialog = true
                return@AuthScreen
            }

            if (password.length < 6) {
                errorMessage = "Password must be at least 6 characters"
                showErrorDialog = true
                return@AuthScreen
            }

            // 🔥 Firebase register
            repo.register(email, password) { success, error ->

                if (success) {
                    navController.navigateAndClearStack("main")
                } else {
                    errorMessage = error ?: "Register failed"
                    showErrorDialog = true
                }
            }
        },
        errorMessage = errorMessage
    )

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Authentication error") },
            text = { Text(errorMessage ?: "Error creating account") },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("Accept")
                }
            }
        )
    }
}