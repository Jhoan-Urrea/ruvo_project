package com.example.ruvo_app.features.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ruvo_app.R
import com.example.ruvo_app.core.theme.Ruvo_appTheme

@Composable
fun LoginScreen(
    onBackClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    onLoginSuccess: () -> Unit = {},
    viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Logo RUVO
            Image(
                painter = painterResource(id = R.drawable.logo_ruvo),
                contentDescription = "RUVO Logo",
                modifier = Modifier.size(120.dp),
                contentScale = ContentScale.Fit
            )
            
            Text(
                text = "RUVO",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 4.sp
                ),
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Iniciar sesión",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp
                ),
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Campo Usuario
            OutlinedTextField(
                value = uiState.email,
                onValueChange = { viewModel.onEmailChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Usuario", color = Color.Gray) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = Color.Gray)
                },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.LightGray,
                    focusedContainerColor = Color(0xFFF8F9FA),
                    unfocusedContainerColor = Color(0xFFF8F9FA)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo Contraseña
            OutlinedTextField(
                value = uiState.password,
                onValueChange = { viewModel.onPasswordChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Contraseña", color = Color.Gray) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color.Gray)
                },
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = null, tint = Color.Gray)
                    }
                },
                shape = RoundedCornerShape(16.dp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.LightGray,
                    focusedContainerColor = Color(0xFFF8F9FA),
                    unfocusedContainerColor = Color(0xFFF8F9FA)
                )
            )

            // Olvidé mi contraseña alineado a la derecha
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onForgotPasswordClick
                ) {
                    Text(
                        text = "Olvidé mi contraseña",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Mensaje de Error
            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botón Ingresar
            Button(
                onClick = { viewModel.login(onLoginSuccess) },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Ingresar",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botón Regresar
            OutlinedButton(
                onClick = onBackClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, Color.LightGray),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
            ) {
                Text(
                    text = "Regresar",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPolishedPreview() {
    Ruvo_appTheme {
        LoginScreen()
    }
}
