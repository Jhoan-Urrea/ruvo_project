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
    @Serializable object Profile : Screen
    @Serializable object Settings : Screen
    @Serializable object EditProfile : Screen
    @Serializable object ModeratorDashboard : Screen
}
