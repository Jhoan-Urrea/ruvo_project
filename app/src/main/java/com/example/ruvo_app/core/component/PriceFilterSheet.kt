package com.example.ruvo_app.core.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceFilterSheet(
    currentMaxPrice: Float?,
    onDismiss: () -> Unit,
    onPriceSelected: (Float?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var sliderPosition by remember { mutableFloatStateOf(currentMaxPrice ?: 1000000f) }
    val format = NumberFormat.getCurrencyInstance(Locale("es", "CO")).apply {
        maximumFractionDigits = 0
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        windowInsets = WindowInsets.navigationBars // Edge-to-Edge: Maneja la barra de navegación gestual
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Filtrar por presupuesto",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Mostrar servicios con precio inicial hasta:",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = format.format(sliderPosition),
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            Slider(
                value = sliderPosition,
                onValueChange = { sliderPosition = it },
                valueRange = 10000f..2000000f,
                steps = 19,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "$ 10k", fontSize = 12.sp, color = Color.Gray)
                Text(text = "$ 2M+", fontSize = 12.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onPriceSelected(sliderPosition) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("Aplicar Filtro", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            
            TextButton(
                onClick = { onPriceSelected(null) },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Limpiar Filtro", color = Color.Gray)
            }
        }
    }
}
