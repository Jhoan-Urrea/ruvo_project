package com.example.ruvo_app

import android.app.Application
import com.cloudinary.android.MediaManager

class RuvoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Configuración de Cloudinary
        val config = mapOf(
            "cloud_name" to "dqf7p5m3v", // Reemplaza con tu cloud_name real de Cloudinary
            "secure" to true
        )
        MediaManager.init(this, config)
    }
}
