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
import androidx.navigation.toRoute
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
import com.example.ruvo_app.features.profile.ProviderProfileScreen
import com.example.ruvo_app.features.chat.ChatScreen
import com.example.ruvo_app.features.service.CrearServicioScreen
import com.example.ruvo_app.features.service.DetalleServicioScreen

@Composable
fun AppNavGraph(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val authState by authViewModel.uiState.collectAsState()

    val startDestination = when (authState) {
        is AuthUiState.Authenticated -> Screen.Dashboard
        else -> Screen.Home
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable<Screen.Home> {
            HomeScreen(onStartClick = { navController.navigate(Screen.AuthSelection) })
        }

        composable<Screen.AuthSelection> {
            AuthSelectionScreen(
                onLoginClick = { navController.navigate(Screen.Login) },
                onRegisterClick = { navController.navigate(Screen.Register) }
            )
        }

        composable<Screen.Login> {
            val loginViewModel: com.example.ruvo_app.features.login.LoginViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return com.example.ruvo_app.features.login.LoginViewModel(
                            LoginUseCase(AuthRepositoryImpl(userRepository = UserRepositoryImpl()))
                        ) as T
                    }
                }
            )
            LoginScreen(
                onBackClick = { navController.popBackStack() },
                onForgotPasswordClick = { navController.navigate(Screen.ForgotPassword) },
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard) {
                        popUpTo(Screen.Home) { inclusive = true }
                    }
                },
                viewModel = loginViewModel
            )
        }

        composable<Screen.ForgotPassword> {
            val forgotPasswordViewModel: com.example.ruvo_app.features.login.ForgotPasswordViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        val authRepository = AuthRepositoryImpl(userRepository = UserRepositoryImpl())
                        return com.example.ruvo_app.features.login.ForgotPasswordViewModel(
                            com.example.ruvo_app.domain.usecase.ResetPasswordUseCase(authRepository)
                        ) as T
                    }
                }
            )
            ForgotPasswordScreen(
                onBackClick = { navController.popBackStack() },
                onCodeSent = { navController.navigate(Screen.ResetPassword) },
                viewModel = forgotPasswordViewModel
            )
        }

        composable<Screen.ResetPassword> {
            ResetPasswordScreen(
                onBackClick = { navController.popBackStack() },
                onResetSuccess = {
                    navController.navigate(Screen.Login) {
                        popUpTo(Screen.AuthSelection) { inclusive = false }
                    }
                }
            )
        }

        composable<Screen.Register> {
            RegisterScreen(
                onBackClick = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Screen.Login) {
                        popUpTo(Screen.AuthSelection) { inclusive = false }
                    }
                }
            )
        }

        composable<Screen.Dashboard> {
            val state = authState as? AuthUiState.Authenticated
            DashboardScreen(
                onLogout = { authViewModel.logout() },
                onSettingsClick = { navController.navigate(Screen.Settings) },
                onAddPostClick = { navController.navigate(Screen.CrearServicio) },
                onAdminDetailedClick = { navController.navigate(Screen.ModeratorDashboard) },
                onServiceClick = { service -> 
                    navController.navigate(service)
                },
                isAdmin = state?.user?.role == UserRole.MODERATOR
            )
        }

        composable<Screen.ModeratorDashboard> {
            ModeratorDashboard(
                onLogout = { authViewModel.logout() },
                onBack = { navController.popBackStack() }
            )
        }

        composable<Screen.Settings> {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onLogoutClick = {
                    authViewModel.logout()
                    navController.navigate(Screen.Home) {
                        popUpTo(Screen.Home) { inclusive = true }
                    }
                },
                onEditProfileClick = {
                    navController.navigate(Screen.EditProfile)
                }
            )
        }

        composable<Screen.EditProfile> {
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

        composable<Screen.CrearServicio> {
            CrearServicioScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<Screen.DetalleServicio> { backStackEntry ->
            val serviceDetail: Screen.DetalleServicio = backStackEntry.toRoute()
            DetalleServicioScreen(
                service = serviceDetail,
                onBackClick = { navController.popBackStack() },
                onViewProfileClick = {
                    navController.navigate(
                        Screen.PerfilProveedor(
                            providerId = serviceDetail.providerId,
                            name = serviceDetail.providerName,
                            specialty = "Especialista", // Podría venir del modelo si existiera
                            rating = serviceDetail.rating,
                            reviewsCount = serviceDetail.reviewsCount,
                            location = serviceDetail.location,
                            imageRes = serviceDetail.providerImageRes
                        )
                    )
                }
            )
        }

        composable<Screen.PerfilProveedor> { backStackEntry ->
            val profileData: Screen.PerfilProveedor = backStackEntry.toRoute()
            ProviderProfileScreen(
                profileData = profileData,
                onBackClick = { navController.popBackStack() },
                onContactClick = {
                    navController.navigate(
                        Screen.Chat(
                            providerId = profileData.providerId,
                            providerName = profileData.name,
                            providerSpecialty = profileData.specialty,
                            providerImageRes = profileData.imageRes,
                            serviceTitle = "Solicitud de Servicio",
                            serviceDescription = "Interés en contactar desde el perfil."
                        )
                    )
                }
            )
        }

        composable<Screen.Chat> { backStackEntry ->
            val chatData: Screen.Chat = backStackEntry.toRoute()
            ChatScreen(
                chatData = chatData,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
