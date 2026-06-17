package com.example.ruvo_app.features.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ruvo_app.core.theme.Ruvo_appTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onBackClick: () -> Unit = {},
    onCodeSent: () -> Unit = {},
    viewModel: ForgotPasswordViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Recuperar contraseña",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                ),
                                modifier = Modifier.padding(end = 48.dp)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.Black
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White
                    ),
                    windowInsets = WindowInsets.statusBars
                )
                HorizontalDivider(color = Color(0xFFF1F3F4))
            }
        },
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0.dp) // Edge-to-Edge: Desactivamos insets automáticos
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Solo considera el TopAppBar
                .imePadding() 
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Icono de sobre
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0xFFE8F0FE), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Email,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = Color(0xFF1A73E8)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "¿Olvidaste tu contraseña?",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                textAlign = TextAlign.Center,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "No te preocupes, te enviaremos instrucciones para restablecerla.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Campo de Email
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Correo electrónico",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = { viewModel.onEmailChanged(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("tu@email.com", color = Color.Gray) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Outlined.Email, contentDescription = null, tint = Color.Gray)
                    },
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.LightGray,
                        focusedContainerColor = Color(0xFFF8F9FA),
                        unfocusedContainerColor = Color(0xFFF8F9FA)
                    ),
                    isError = uiState.error != null
                )
                
                Text(
                    text = "Ingresa el correo asociado a tu cuenta de RUVO",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp)
                )

                if (uiState.error != null) {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { 
                    viewModel.onResetPasswordClicked {
                        // Éxito se maneja con el estado isEmailSent
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text(
                        text = "Enviar enlace de recuperación",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider(color = Color(0xFFF1F3F4))
            Spacer(modifier = Modifier.height(32.dp))

            // Consejos Útiles
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF8F9FA))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Lightbulb,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Consejos útiles",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.Black
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                val tips = listOf(
                    "Verifica que tu correo esté escrito correctamente",
                    "Revisa tu carpeta de spam o correo no deseado",
                    "El enlace de recuperación expira en 24 horas",
                    "Por seguridad, no compartas el enlace con nadie"
                )
                tips.forEach { tip ->
                    Row(modifier = Modifier.padding(bottom = 4.dp)) {
                        Text(text = "•", color = Color.Gray)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = tip,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Sección Inferior
            Text(
                text = "¿Recordaste tu contraseña?",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(8.dp))

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
                    text = "Volver al inicio de sesión",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            
            Spacer(modifier = Modifier.navigationBarsPadding()) // Margen para barra de navegación
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (uiState.isEmailSent) {
            AlertDialog(
                onDismissRequest = { /* Handle dismiss */ },
                confirmButton = {
                    TextButton(onClick = onCodeSent) {
                        Text("Aceptar")
                    }
                },
                title = { Text("Correo enviado") },
                text = { Text("Se ha enviado un correo con las instrucciones para restablecer tu contraseña.") }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ForgotPasswordScreenPreview() {
    Ruvo_appTheme {
        ForgotPasswordScreen()
    }
}
