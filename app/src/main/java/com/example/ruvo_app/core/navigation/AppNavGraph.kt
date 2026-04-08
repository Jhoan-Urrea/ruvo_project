package com.example.ruvo_app.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ruvo_app.data.repository.AuthRepositoryImpl
import com.example.ruvo_app.data.repository.UserRepositoryImpl
import com.example.ruvo_app.domain.model.UserRole
import com.example.ruvo_app.domain.usecase.LoginUseCase
import com.example.ruvo_app.domain.usecase.UpdateUserProfileUseCase
import com.example.ruvo_app.features.auth.AuthUiState
import com.example.ruvo_app.features.auth.AuthViewModel
import com.example.ruvo_app.features.dashboard.DashboardScreen
import com.example.ruvo_app.features.dashboard.ModeratorDashboard
import com.example.ruvo_app.features.home.HomeScreen
import com.example.ruvo_app.features.login.AuthSelectionScreen
import com.example.ruvo_app.features.login.ForgotPasswordScreen
import com.example.ruvo_app.features.login.LoginScreen
import com.example.ruvo_app.features.login.ResetPasswordScreen
import com.example.ruvo_app.features.register.RegisterScreen
import com.example.ruvo_app.features.settings.EditProfileScreen
import com.example.ruvo_app.features.settings.EditProfileViewModel
import com.example.ruvo_app.features.settings.SettingsScreen

@Composable
fun AppNavGraph(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val authState by authViewModel.uiState.collectAsState()

    val startDestination = when (val state = authState) {
        is AuthUiState.Authenticated -> Screen.Dashboard.route
        else -> Screen.Home.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Home.route) {
            HomeScreen(onStartClick = { navController.navigate(Screen.AuthSelection.route) })
        }

        composable(Screen.AuthSelection.route) {
            AuthSelectionScreen(
                onLoginClick = { navController.navigate(Screen.Login.route) },
                onRegisterClick = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onBackClick = { navController.popBackStack() },
                onForgotPasswordClick = { navController.navigate(Screen.ForgotPassword.route) },
                viewModel = com.example.ruvo_app.features.login.LoginViewModel(
                    LoginUseCase(AuthRepositoryImpl(userRepository = UserRepositoryImpl()))
                )
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onBackClick = { navController.popBackStack() },
                onCodeSent = { navController.navigate(Screen.ResetPassword.route) }
            )
        }

        composable(Screen.ResetPassword.route) {
            ResetPasswordScreen(
                onBackClick = { navController.popBackStack() },
                onResetSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.AuthSelection.route) { inclusive = false }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Screen.Dashboard.route) {
            val state = authState as? AuthUiState.Authenticated
            DashboardScreen(
                onLogout = { authViewModel.logout() },
                onSettingsClick = { navController.navigate(Screen.Settings.route) },
                onAdminDetailedClick = { navController.navigate(Screen.ModeratorDashboard.route) },
                isAdmin = state?.user?.role == UserRole.MODERATOR
            )
        }

        composable(Screen.ModeratorDashboard.route) {
            ModeratorDashboard(
                onLogout = { authViewModel.logout() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onLogoutClick = {
                    authViewModel.logout()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onEditProfileClick = {
                    navController.navigate(Screen.EditProfile.route)
                }
            )
        }

        composable(Screen.EditProfile.route) {
            val editViewModel: EditProfileViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return EditProfileViewModel(UpdateUserProfileUseCase(UserRepositoryImpl())) as T
                    }
                }
            )
            EditProfileScreen(
                onBackClick = { navController.popBackStack() },
                viewModel = editViewModel
            )
        }
    }
}