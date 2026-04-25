package com.example.ruvo_app.core.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable object Home : Screen
    @Serializable object AuthSelection : Screen
    @Serializable object Login : Screen
    @Serializable object Register : Screen
    @Serializable object ForgotPassword : Screen
    @Serializable object ResetPassword : Screen
    @Serializable data class Dashboard(val successMessage: String? = null) : Screen
    @Serializable object Search : Screen
    @Serializable object Profile : Screen
    @Serializable object Notifications : Screen
    @Serializable object Settings : Screen
    @Serializable object EditProfile : Screen
    @Serializable data class PerfilProveedor(
        val providerId: String,
        val name: String,
        val specialty: String,
        val rating: Float,
        val reviewsCount: Int,
        val location: String,
        val imageRes: Int
    ) : Screen
    @Serializable data class Chat(
        val providerId: String,
        val providerName: String,
        val providerSpecialty: String,
        val providerImageRes: Int,
        val serviceTitle: String? = null,
        val serviceDescription: String? = null
    ) : Screen
    @Serializable object ChatList : Screen
    @Serializable object ModeratorDashboard : Screen
    @Serializable object CrearServicio : Screen
    @Serializable data class DetalleServicio(
        val id: String,
        val title: String,
        val description: String,
        val category: String,
        val location: String,
        val priceRange: String,
        val providerId: String,
        val providerName: String,
        val providerSpecialty: String,
        val providerImageRes: Int,
        val rating: Float = 0f,
        val reviewsCount: Int = 0,
        val imageRes: Int
    ) : Screen
    @Serializable data class SolicitarServicio(
        val serviceId: String,
        val serviceTitle: String,
        val serviceCategory: String,
        val servicePriceRange: String,
        val serviceImageRes: Int,
        val providerName: String,
        val providerSpecialty: String,
        val providerImageRes: Int,
        val location: String
    ) : Screen
}
