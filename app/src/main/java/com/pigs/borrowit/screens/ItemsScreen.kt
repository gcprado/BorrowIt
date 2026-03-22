package com.pigs.borrowit.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.pigs.borrowit.screens.components.MainBottomNav
import com.pigs.borrowit.screens.components.UploadItemDialog

@Composable
fun ItemsScreen(
    navController: NavController
) {

    var showAddItem by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Pantalla de Items",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Esta es la pantalla para los items",
                style = MaterialTheme.typography.bodyLarge
            )
            Button(
                onClick = {
                    showAddItem = true
                }
            ) {
                Text(text = "Añadir nuevo objeto")
            }

            if (showAddItem) {
                UploadItemDialog(
                    onDismiss = {
                        showAddItem = false
                    }
                )
            }
        }
        MainBottomNav(navController, modifier = Modifier.align(Alignment.BottomCenter))
    }
}