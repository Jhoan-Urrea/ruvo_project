package com.example.ruvo_app.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ruvo_app.features.home.HomeScreen
import com.example.ruvo_app.features.login.AuthSelectionScreen
import com.example.ruvo_app.features.login.LoginScreen
import com.example.ruvo_app.features.register.RegisterScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onStartClick = {
                    navController.navigate(Screen.AuthSelection.route)
                }
            )
        }

        composable(Screen.AuthSelection.route) {
            AuthSelectionScreen(
                onLoginClick = {
                    navController.navigate(Screen.Login.route)
                },
                onRegisterClick = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}