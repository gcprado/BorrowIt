package com.pigs.borrowit.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.pigs.borrowit.data.repositories.ItemRepository
import com.pigs.borrowit.screens.components.MainBottomNav
import com.pigs.borrowit.screens.components.UploadItemDialog

@Composable
fun ItemsScreen(
    navController: NavController
) {
    val repository = remember { ItemRepository() }
    val currentUser = remember { FirebaseAuth.getInstance().currentUser }
    
    // Obtenemos solo los items cuyo 'owner' coincide con el UID del usuario actual
    val itemsState by remember(currentUser) {
        repository.getItemsByOwnerFlow(currentUser?.uid ?: "")
    }.collectAsState(initial = null)
    
    var showAddItem by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Mis Items",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )

            Button(
                onClick = { showAddItem = true },
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(text = "Añadir nuevo objeto")
            }

            when (val items = itemsState) {
                null -> {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                else -> {
                    if (items.isEmpty()) {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text("No tienes items publicados")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            items(items) { item ->
                                Text(text = "${item.name}: ${item.description}")
                            }
                        }
                    }
                }
            }

            if (showAddItem) {
                UploadItemDialog(
                    userId = currentUser?.uid ?: "anonymous",
                    onDismiss = { showAddItem = false },
                    onItemUploaded = { _ ->
                        showAddItem = false
                    }
                )
            }
        }
        MainBottomNav(navController, modifier = Modifier.align(Alignment.BottomCenter))
    }
}
