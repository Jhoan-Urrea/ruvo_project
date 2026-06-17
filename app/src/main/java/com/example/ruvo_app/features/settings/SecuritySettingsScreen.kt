package com.example.ruvo_app.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySettingsScreen(
    onBackClick: () -> Unit,
    onChangePasswordClick: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Seguridad",
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
                .background(Color(0xFFF8F9FA))
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Protección de tu cuenta",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.padding(start = 8.dp)
            )

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Column {
                    SettingsItem(
                        icon = Icons.Outlined.Lock,
                        title = "Cambiar Contraseña",
                        subtitle = "Actualiza tu clave de acceso",
                        onClick = onChangePasswordClick
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF1F3F5))
                    SettingsItem(
                        icon = Icons.Outlined.Shield,
                        title = "Verificación en dos pasos",
                        subtitle = "Próximamente disponible",
                        onClick = { /* TODO */ }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE7F5FF)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Shield, contentDescription = null, tint = Color(0xFF228BE6))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Tu cuenta está protegida con los estándares de seguridad de Ruvo.",
                        fontSize = 13.sp,
                        color = Color(0xFF1971C2)
                    )
                }
            }

            Spacer(modifier = Modifier.navigationBarsPadding())
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
