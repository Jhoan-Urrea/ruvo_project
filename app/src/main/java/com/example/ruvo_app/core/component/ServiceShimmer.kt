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
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Imagen principal
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .shimmerEffect()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Título
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(20.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Descripción
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Fila inferior (Avatar + Precio)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .shimmerEffect()
            )
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(18.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect()
            )
        }
    }
}

@Composable
fun DashboardShimmer() {
    Column(modifier = Modifier.fillMaxSize()) {
        repeat(3) {
            ServiceCardShimmer()
        }
    }
}
