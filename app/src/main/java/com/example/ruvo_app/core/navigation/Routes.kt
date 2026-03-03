package com.example.ruvo_app.core.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AuthSelection : Screen("auth_selection")
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object ResetPassword : Screen("reset_password")
}