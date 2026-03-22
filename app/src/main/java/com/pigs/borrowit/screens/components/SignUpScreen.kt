package com.pigs.borrowit.screens.components

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
import com.pigs.borrowit.presentation.navigation.navigateAndClearStack
import com.pigs.borrowit.screens.AuthScreen

@Composable
fun SignUpScreen(navController: NavController) {

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showErrorDialog by remember { mutableStateOf(false) }

    AuthScreen(
        mode = AuthMode.SIGNUP,
        onSwitchMode = { navController.popBackStack() },
        onSubmit = { username, email, password ->
            // lógica registro
            if (handleRegister(username, email, password)) {
                navController.navigateAndClearStack("main")
                errorMessage = null
                showErrorDialog = false
            } else {
                showErrorDialog = true
            }
        },
        errorMessage = errorMessage
    )

    // AlertDialog que aparece como popup
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Authentication error") },
            text = { Text("Error creating account. Please check your information and try again.") },
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

fun handleRegister(username: String, email: String, password: String): Boolean {
    //TODO: Implementar registro en firestore
    return false
}