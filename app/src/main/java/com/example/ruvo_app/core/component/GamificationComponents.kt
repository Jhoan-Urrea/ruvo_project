package com.example.ruvo_app.core.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LogrosSection(badges: List<String>, title: String = "Mis Logros", emptyMessage: String = "¡Empieza a explorar para ganar insignias!") {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color.Gray)
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        if (badges.isEmpty()) {
            Text(
                text = emptyMessage,
                style = MaterialTheme.typography.bodySmall,
                color = Color.LightGray,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(end = 24.dp)
            ) {
                items(badges) { badgeName ->
                    InsigniaItem(badgeName)
                }
            }
        }
    }
}

@Composable
fun InsigniaItem(name: String) {
    val emoji = when (name) {
        "Bienvenido a Ruvo" -> "🎉"
        "Primer Contacto" -> "💬"
        "Emprendedor" -> "🚀"
        "Popular" -> "🔥"
        "Mano de Obra" -> "🛠️"
        "Explorador" -> "🗺️"
        "Crítico" -> "✍️"
        else -> "🏅"
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(70.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(Color(0xFFF0F4FF), CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 28.sp)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 11.sp,
            color = Color.DarkGray
        )
    }
}
