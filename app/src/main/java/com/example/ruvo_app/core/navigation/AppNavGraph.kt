package com.example.ruvo_app.core.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ruvo_app.data.repository.AuthRepositoryImpl
import com.example.ruvo_app.domain.usecase.LoginUseCase
import com.example.ruvo_app.domain.usecase.RegisterUseCase
import com.example.ruvo_app.domain.usecase.ResetPasswordUseCase
import com.example.ruvo_app.features.home.HomeScreen
import com.example.ruvo_app.features.login.*
import com.example.ruvo_app.features.register.RegisterScreen
import com.example.ruvo_app.features.register.RegisterViewModel
import com.example.ruvo_app.features.dashboard.DashboardScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val authRepository = AuthRepositoryImpl()

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
            val viewModel: LoginViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return LoginViewModel(LoginUseCase(authRepository)) as T
                    }
                }
            )
            LoginScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onForgotPasswordClick = {
                    navController.navigate(Screen.ForgotPassword.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                viewModel = viewModel
            )
        }

        composable(Screen.ForgotPassword.route) {
            val viewModel: ForgotPasswordViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return ForgotPasswordViewModel(ResetPasswordUseCase(authRepository)) as T
                    }
                }
            )
            ForgotPasswordScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onCodeSent = {
                    navController.navigate(Screen.ResetPassword.route)
                },
                viewModel = viewModel
            )
        }

        composable(Screen.ResetPassword.route) {
            ResetPasswordScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onResetSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.AuthSelection.route) { inclusive = false }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            val viewModel: RegisterViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return RegisterViewModel(RegisterUseCase(authRepository)) as T
                    }
                }
            )
            RegisterScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                viewModel = viewModel
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen()
        }
    }
}