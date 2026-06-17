package com.example.ruvo_app.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyTermsScreen(
    onBackClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Privacidad y Términos",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White),
                windowInsets = WindowInsets.statusBars // Edge-to-Edge: Barra de estado
            )
        },
        contentWindowInsets = WindowInsets(0.dp) // Control manual de insets
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Términos de Servicio",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Al usar Ruvo, aceptas cumplir con nuestras normas de comunidad. Queda prohibida la publicación de servicios ilegales, fraudulentos o que atenten contra la integridad de los usuarios.",
                fontSize = 14.sp,
                lineHeight = 22.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Política de Privacidad",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "En Ruvo, nos tomamos en serio tu privacidad. Recopilamos datos básicos como tu nombre, teléfono y ubicación para facilitar la conexión entre proveedores y clientes. Tus datos nunca serán compartidos con terceros sin tu consentimiento explícito.",
                fontSize = 14.sp,
                lineHeight = 22.sp,
                color = Color.DarkGray
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Uso de Localización",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "La aplicación utiliza tu ubicación para mostrarte servicios cercanos y optimizar el radio de cobertura de tus propias publicaciones.",
                fontSize = 14.sp,
                lineHeight = 22.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = "Última actualización: Mayo 2024",
                fontSize = 12.sp,
                color = Color.Gray
            )
            
            // Margen para la barra de navegación gestual
            Spacer(modifier = Modifier.navigationBarsPadding())
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
