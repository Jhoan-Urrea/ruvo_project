package com.example.ruvo_app

import android.app.Application
import com.cloudinary.android.MediaManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RuvoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Configuración de Cloudinary (Requisito Académico: URLs fijas permitidas, 
        // pero mantenemos el manager si ya está configurado)
        try {
            val config = mapOf(
                "cloud_name" to "dqf7p5m3v",
                "secure" to true
            )
            MediaManager.init(this, config)
        } catch (e: Exception) {
            // Ya inicializado o error de config
        }
    }
}
