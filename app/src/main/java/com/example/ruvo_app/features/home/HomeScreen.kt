package com.example.ruvo_app.features.home

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ruvo_app.R
import com.example.ruvo_app.core.theme.Ruvo_appTheme

@Composable
fun HomeScreen(
    onStartClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background // Fondo total de la pantalla
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding() // Solo el contenido respeta la barra de estado
                .navigationBarsPadding() // Solo el contenido respeta la barra de navegación
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Contenido Central: Logo y Eslogan
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_ruvo),
                    contentDescription = "Ruvo Logo",
                    modifier = Modifier.size(180.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Conecta con servicios\nconfiables cerca de ti",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp,
                        lineHeight = 24.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Acción Inferior: Botón Empezar
            Button(
                onClick = onStartClick,
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
                    text = "Empezar",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    Ruvo_appTheme {
        HomeScreen(onStartClick = {})
    }
}
