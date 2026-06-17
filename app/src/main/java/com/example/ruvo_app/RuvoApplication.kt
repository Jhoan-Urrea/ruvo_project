package com.example.ruvo_app

import android.app.Application
import android.util.Log
import com.cloudinary.android.MediaManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RuvoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        try {
            val config = mapOf(
                "cloud_name" to "dnii70z8e", // Actualizado al valor correcto
                "secure" to true
            )
            MediaManager.init(this, config)
            Log.d("RUVO_APP", "Cloudinary inicializado correctamente con cloud_name: dnii70z8e")
        } catch (e: Exception) {
            Log.e("RUVO_APP", "Error al inicializar Cloudinary: ${e.message}")
        }
    }
}
