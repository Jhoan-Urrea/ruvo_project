package com.example.ruvo_app.features.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.ruvo_app.R

@Composable
fun SplashScreen() {
    // La SplashScreen debe ser 100% inmersiva
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_ruvo),
            contentDescription = "Logo",
            modifier = Modifier.size(160.dp),
            contentScale = ContentScale.Fit
        )
    }
}
