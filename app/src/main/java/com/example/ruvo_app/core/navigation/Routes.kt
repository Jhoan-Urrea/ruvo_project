package com.example.ruvo_app.core.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable object Home : Screen
    @Serializable object AuthSelection : Screen
    @Serializable object Login : Screen
    @Serializable object Register : Screen
    @Serializable object ForgotPassword : Screen
    @Serializable object ResetPassword : Screen
    @Serializable object Dashboard : Screen
    @Serializable object Search : Screen
    @Serializable object Profile : Screen
    @Serializable object Settings : Screen
    @Serializable object EditProfile : Screen
    @Serializable object ModeratorDashboard : Screen
    @Serializable object CrearServicio : Screen
    @Serializable data class DetalleServicio(
        val id: String,
        val title: String,
        val description: String,
        val category: String,
        val location: String,
        val priceRange: String,
        val userName: String,
        val userSpecialty: String,
        val imageRes: Int
    ) : Screen
}
