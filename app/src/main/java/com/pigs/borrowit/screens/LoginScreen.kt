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
import com.pigs.borrowit.data.AuthRepository
import com.pigs.borrowit.presentation.navigation.navigateAndClearStack

@Composable
fun LoginScreen(navController: NavController) {

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showErrorDialog by remember { mutableStateOf(false) }

    val repo = AuthRepository()

    AuthScreen(
        mode = AuthMode.LOGIN,
        onSwitchMode = { navController.navigate("signup") },
        onSubmit = { email, _, password ->

            // Validación
            if (email.isBlank() || password.isBlank()) {
                errorMessage = "Fields cannot be empty"
                showErrorDialog = true
                return@AuthScreen
            }

            // Firebase login
            repo.login(email, password) { success, error ->

                if (success) {
                    navController.navigateAndClearStack("main")
                } else {
                    errorMessage = error ?: "Login failed"
                    showErrorDialog = true
                }
            }
        },
        errorMessage = errorMessage
    )

    // AlertDialog que aparece como popup
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Authentication error") },
            text = { Text("Invalid username or password. Please try again.") },
            confirmButton = {
                TextButton(
                    onClick = { showErrorDialog = false }
                ) {
                    Text("Accept")
                }
            }
        )
    }
}
