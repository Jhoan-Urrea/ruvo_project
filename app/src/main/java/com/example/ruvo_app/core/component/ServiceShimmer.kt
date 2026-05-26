package com.example.ruvo_app.core.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun ServiceCardShimmer() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Imagen principal inmersiva
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .shimmerEffect()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Título (Placeholder)
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Descripción (Placeholder)
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Fila inferior (Avatar + Precio)
        Row(
            modifier = Modifier.fillMaxWidth(), 
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .shimmerEffect()
            )
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect()
            )
        }
    }
}

@Composable
fun DashboardShimmer() {
    // Column sin fillMaxSize para respetar el scroll del contenedor padre
    Column(modifier = Modifier.fillMaxWidth()) {
        repeat(4) {
            ServiceCardShimmer()
        }
    }
}
