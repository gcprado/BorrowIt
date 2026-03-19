package com.pigs.borrowit.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.pigs.borrowit.R
import com.pigs.borrowit.control.AuthMode
import com.pigs.borrowit.ui.theme.Background
import com.pigs.borrowit.ui.theme.Border
import com.pigs.borrowit.ui.theme.CardBackground
import com.pigs.borrowit.ui.theme.Primary
import com.pigs.borrowit.ui.theme.PrimaryLight
import com.pigs.borrowit.ui.theme.TextSecondary

@Composable
fun AuthScreen(
    mode: AuthMode,
    onSwitchMode: () -> Unit,
    onSubmit: (username: String, email: String, password: String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {

        Spacer(modifier = Modifier.height(125.dp))

        Surface(
            color = CardBackground,
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                
                Image(
                    painter = painterResource(id = R.drawable.borrowitlogo),
                    contentDescription = "BorrowIt Logo",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(16.dp))
                )

                Spacer(modifier = Modifier.height(24.dp))

                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(15))
                        .background(PrimaryLight)
                ) {
                    Button(
                        onClick = { if (mode != AuthMode.LOGIN) onSwitchMode() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(15),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (mode == AuthMode.LOGIN) Primary else PrimaryLight,
                            contentColor = if (mode == AuthMode.LOGIN) Color.Black else TextSecondary
                        ),
                        elevation = null
                    ) {
                        Text("Log In")
                    }

                    Button(
                        onClick = { if (mode != AuthMode.SIGNUP) onSwitchMode() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(15),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (mode == AuthMode.SIGNUP) Primary else PrimaryLight,
                            contentColor = if (mode == AuthMode.SIGNUP) Color.Black else TextSecondary
                        ),
                        elevation = null
                    ) {
                        Text("Sign Up")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                
                if (mode == AuthMode.SIGNUP) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        placeholder = { Text("Enter your username") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                
                if (mode == AuthMode.SIGNUP) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("Enter a valid email") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                
                if (mode == AuthMode.LOGIN) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        placeholder = { Text("Enter username or email") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Password") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible)
                                    Icons.Filled.VisibilityOff
                                else
                                    Icons.Filled.Visibility,
                                contentDescription = if (passwordVisible)
                                    "Ocultar contraseña"
                                else
                                    "Mostrar contraseña"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                
                if (mode == AuthMode.SIGNUP) {
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        placeholder = { Text("Please enter your password again") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = if (confirmPasswordVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible)
                                        Icons.Filled.VisibilityOff
                                    else
                                        Icons.Filled.Visibility,
                                    contentDescription = if (confirmPasswordVisible)
                                        "Ocultar contraseña"
                                    else
                                        "Mostrar contraseña"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                
                Button(
                    onClick = {
                        onSubmit(username, email, password)
                    },
                    shape = RoundedCornerShape(15),
                    modifier = Modifier.width(200.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = Color.Black
                    )
                ) {
                    Text(if (mode == AuthMode.LOGIN) "Log In" else "Sign Up")
                }

                Spacer(modifier = Modifier.height(16.dp))

                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Divider(modifier = Modifier.weight(1f))
                    Text("OR", modifier = Modifier.padding(horizontal = 8.dp))
                    Divider(modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(16.dp))

                
                OutlinedButton(
                    onClick = { },
                    shape = RoundedCornerShape(15),
                    modifier = Modifier.width(200.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Primary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Border)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.googlelogo),
                        contentDescription = "Google Logo",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}