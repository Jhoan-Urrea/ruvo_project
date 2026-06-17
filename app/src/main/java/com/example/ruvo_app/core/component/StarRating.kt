package com.example.ruvo_app.core.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun StarRatingBar(
    rating: Float,
    onRatingChanged: (Int) -> Unit = {},
    selectable: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        repeat(5) { index ->
            val starIndex = index + 1
            val icon = when {
                starIndex <= rating -> Icons.Default.Star
                starIndex - 0.5f <= rating -> Icons.Default.StarHalf
                else -> Icons.Default.StarBorder
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFFFFC107),
                modifier = Modifier
                    .size(24.dp)
                    .then(if (selectable) Modifier.clickable { onRatingChanged(starIndex) } else Modifier)
            )
        }
    }
}
