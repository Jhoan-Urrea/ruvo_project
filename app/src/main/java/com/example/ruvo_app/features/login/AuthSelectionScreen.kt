package com.example.ruvo_app.features.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ruvo_app.R
import com.example.ruvo_app.core.theme.Ruvo_appTheme

@Composable
fun AuthSelectionScreen(
    onLoginClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White // Fondo total de la pantalla
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Contenido Central: Logo (sin systemBarsPadding para que pueda subir más si es necesario)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .statusBarsPadding(), // Solo el logo respeta la barra de estado si está muy arriba
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_ruvo),
                    contentDescription = "Ruvo Logo",
                    modifier = Modifier.size(180.dp),
                    contentScale = ContentScale.Fit
                )
            }

            // Botones de acción inferiores
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding() // Asegura que los botones no queden tras la barra de navegación
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Botón Iniciar Sesión
                Button(
                    onClick = onLoginClick,
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = "Iniciar sesión",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                // Botón Registrarme
                OutlinedButton(
                    onClick = onRegisterClick,
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, Color.LightGray),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = "Registrarme",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AuthSelectionScreenPreview() {
    Ruvo_appTheme {
        AuthSelectionScreen()
    }
}
