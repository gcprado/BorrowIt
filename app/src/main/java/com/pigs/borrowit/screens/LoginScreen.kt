package com.pigs.borrowit.screens

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pigs.borrowit.control.AuthMode
import com.pigs.borrowit.control.AuthViewModel
import com.pigs.borrowit.presentation.navigation.navigateAndClearStack

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel()
) {
    val errorMessage by viewModel.errorMessage.collectAsState()
    val showErrorDialog by viewModel.showErrorDialog.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            navController.navigateAndClearStack("main")
            viewModel.onNavigationHandled()
        }
    }

    AuthScreen(
        mode = AuthMode.LOGIN,
        onSwitchMode = { navController.navigate("signup") },
        onSubmit = { _, email, password ->
            viewModel.login(email, password)
        },
        onGoogleSignIn = { credential ->
            viewModel.signInWithGoogle(credential)
        },
        errorMessage = errorMessage
    )

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Authentication error") },
            text = { Text(errorMessage ?: "Invalid username or password. Please try again.") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("Accept")
                }
            }
        )
    }
}
