package com.pigs.borrowit.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.pigs.borrowit.screens.components.MainBottomNav

@Composable
fun CommsScreen(
    navController: NavController
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Pantalla de Comunidades",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Esta es la pantalla de comunidades",
            style = MaterialTheme.typography.bodyLarge
        )
    }
    MainBottomNav(navController)
}