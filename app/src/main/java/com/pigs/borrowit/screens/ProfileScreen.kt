package com.pigs.borrowit.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pigs.borrowit.R
import com.pigs.borrowit.screens.components.MainBottomNav
import com.pigs.borrowit.ui.theme.Background
import com.pigs.borrowit.ui.theme.CardBackground
import com.pigs.borrowit.ui.theme.Primary

@Composable
fun ProfileScreen(
    navController: NavController,
    onLogout: () -> Unit = {},
    onDeleteAccount: () -> Unit = {},
    onChangePhoto: () -> Unit = {},
    onSaveChanges: (String) -> Unit = {},
    initialUsername: String = "john_doe",
    email: String = "person@domain.com"
) {
    var username by remember { mutableStateOf(initialUsername) }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(80.dp))

            // 🟡 CARD 1 → PERFIL (Primary)
            Surface(
                color = Primary,
                shape = RoundedCornerShape(24.dp),
                tonalElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Box(
                        modifier = Modifier.size(120.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            shape = CircleShape,
                            color = Color.Transparent,
                            border = BorderStroke(2.dp, Color.LightGray)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.profilepicture_default),
                                contentDescription = "Profile Image",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }

                        FloatingActionButton(
                            onClick = onChangePhoto,
                            modifier = Modifier.size(36.dp),
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = email,
                        fontSize = 14.sp,
                        color = Color.Black.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        placeholder = { Text("Enter your username") },
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 16.sp
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color.Gray,        // cuando está seleccionado
                            unfocusedBorderColor = Primary,   // cuando NO está seleccionado
                            cursorColor = Primary,               // color del cursor
                            focusedLabelColor = Primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ⚪ CARD 2 → BOTONES (blanco)
            Surface(
                color = CardBackground,
                shape = RoundedCornerShape(24.dp),
                tonalElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {

                    Button(
                        onClick = { onSaveChanges(username) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Save Changes")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onLogout,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE5D1B0),
                            contentColor = Color.Black
                        )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Log Out")
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.Logout, contentDescription = null)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onDeleteAccount,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE19F9F),
                            contentColor = Color.Black
                        )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Delete Account")
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.Delete, contentDescription = null)
                        }
                    }
                }
            }
        }
        MainBottomNav(navController, modifier = Modifier.align(Alignment.BottomCenter))
    }
}