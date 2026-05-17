package com.example.ruvo_app.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.ruvo_app.domain.model.UserRole
import com.example.ruvo_app.features.auth.AuthUiState
import com.example.ruvo_app.features.auth.AuthViewModel
import com.example.ruvo_app.features.dashboard.DashboardScreen
import com.example.ruvo_app.features.dashboard.DashboardViewModel
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
import com.example.ruvo_app.features.chat.ChatListScreen
import com.example.ruvo_app.features.request.SolicitarServicioScreen
import com.example.ruvo_app.features.request.MisSolicitudesScreen
import com.example.ruvo_app.features.request.MisTrabajosScreen
import com.example.ruvo_app.features.notifications.NotificationsScreen
import com.example.ruvo_app.features.service.CrearServicioScreen
import com.example.ruvo_app.features.service.ServiceDetailScreen
import com.example.ruvo_app.features.service.SelectLocationScreen
import com.example.ruvo_app.R

@Composable
fun AppNavGraph(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val authState by authViewModel.uiState.collectAsState()

    val startDestination: Screen = when (authState) {
        is AuthUiState.Authenticated -> Screen.Dashboard()
        else -> Screen.Home
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable<Screen.Home> {
            HomeScreen(onStartClick = { navController.navigate(Screen.AuthSelection) })
        }

        with(navController) {
            composable<Screen.AuthSelection> {
                AuthSelectionScreen(
                    onLoginClick = { navigate(Screen.Login) },
                    onRegisterClick = { navigate(Screen.Register) }
                )
            }

            composable<Screen.Login> {
                val loginViewModel: com.example.ruvo_app.features.login.LoginViewModel = hiltViewModel()
                LoginScreen(
                    onBackClick = { popBackStack() },
                    onForgotPasswordClick = { navigate(Screen.ForgotPassword) },
                    onLoginSuccess = {
                        navigate(Screen.Dashboard()) {
                            popUpTo(Screen.Home) { inclusive = true }
                        }
                    },
                    viewModel = loginViewModel
                )
            }

            composable<Screen.ForgotPassword> {
                val forgotPasswordViewModel: com.example.ruvo_app.features.login.ForgotPasswordViewModel = hiltViewModel()
                ForgotPasswordScreen(
                    onBackClick = { popBackStack() },
                    onCodeSent = { navigate(Screen.ResetPassword) },
                    viewModel = forgotPasswordViewModel
                )
            }

            composable<Screen.ResetPassword> {
                ResetPasswordScreen(
                    onBackClick = { popBackStack() },
                    onResetSuccess = {
                        navigate(Screen.Login) {
                            popUpTo(Screen.AuthSelection) { inclusive = false }
                        }
                    }
                )
            }

            composable<Screen.Register> {
                RegisterScreen(
                    onBackClick = { popBackStack() },
                    onRegisterSuccess = {
                        navigate(Screen.Dashboard()) {
                            popUpTo(Screen.Home) { inclusive = true }
                        }
                    }
                )
            }

            composable<Screen.Dashboard> { backStackEntry ->
                val dashboardData: Screen.Dashboard = backStackEntry.toRoute()
                val state = authState as? AuthUiState.Authenticated
                val dashboardViewModel: DashboardViewModel = hiltViewModel()
                
                DashboardScreen(
                    onLogout = { authViewModel.logout() },
                    onSettingsClick = { navigate(Screen.Settings) },
                    onAddPostClick = { navigate(Screen.CrearServicio) },
                    onAdminDetailedClick = { navigate(Screen.ModeratorDashboard) },
                    onServiceClick = { service -> 
                        navigate(service)
                    },
                    onChatListClick = { navigate(Screen.ChatList) },
                    onSolicitudesClick = { navigate(Screen.MisSolicitudes) },
                    onMisTrabajosClick = { navigate(Screen.MisTrabajos) },
                    isAdmin = state?.user?.role == UserRole.MODERATOR,
                    initialSuccessMessage = dashboardData.successMessage,
                    viewModel = dashboardViewModel
                )
            }

            composable<Screen.ChatList> {
                ChatListScreen(
                    onBackClick = { popBackStack() },
                    onChatClick = { chatId, userName, role, imageRes, imageUrl: String? ->
                        navigate(
                            Screen.Chat(
                                providerId = chatId,
                                providerName = userName,
                                providerSpecialty = role,
                                providerImageRes = imageRes,
                                providerImageUrl = imageUrl
                            )
                        )
                    }
                )
            }

            composable<Screen.ModeratorDashboard> {
                ModeratorDashboard(
                    onLogout = { authViewModel.logout() },
                    onBack = { popBackStack() }
                )
            }

            composable<Screen.Settings> {
                SettingsScreen(
                    onBackClick = { popBackStack() },
                    onLogoutClick = {
                        authViewModel.logout()
                        navigate(Screen.Home) {
                            popUpTo(Screen.Home) { inclusive = true }
                        }
                    },
                    onEditProfileClick = {
                        navigate(Screen.EditProfile)
                    }
                )
            }

            composable<Screen.Notifications> {
                NotificationsScreen()
            }

            composable<Screen.EditProfile> {
                val editViewModel: EditProfileViewModel = hiltViewModel()
                EditProfileScreen(
                    onBackClick = { popBackStack() },
                    viewModel = editViewModel
                )
            }

            composable<Screen.CrearServicio> { backStackEntry ->
                val selectedLat = backStackEntry.savedStateHandle.get<Double>("lat")
                val selectedLng = backStackEntry.savedStateHandle.get<Double>("lng")
                val selectedAddress = backStackEntry.savedStateHandle.get<String>("address")
                val country = backStackEntry.savedStateHandle.get<String>("country")
                val region = backStackEntry.savedStateHandle.get<String>("region")
                val city = backStackEntry.savedStateHandle.get<String>("city")
                val exact = backStackEntry.savedStateHandle.get<String>("exact")
                
                CrearServicioScreen(
                    onBackClick = { popBackStack() },
                    onSelectLocationClick = { navigate(Screen.SelectLocation) },
                    initialLat = selectedLat,
                    initialLng = selectedLng,
                    initialAddress = selectedAddress,
                    initialCountry = country,
                    initialRegion = region,
                    initialCity = city,
                    initialExact = exact
                )
            }

            composable<Screen.SelectLocation> {
                SelectLocationScreen(
                    onLocationSelected = { lat, lng, full, country, region, city, exact ->
                        previousBackStackEntry?.savedStateHandle?.set("lat", lat)
                        previousBackStackEntry?.savedStateHandle?.set("lng", lng)
                        previousBackStackEntry?.savedStateHandle?.set("address", full)
                        previousBackStackEntry?.savedStateHandle?.set("country", country)
                        previousBackStackEntry?.savedStateHandle?.set("region", region)
                        previousBackStackEntry?.savedStateHandle?.set("city", city)
                        previousBackStackEntry?.savedStateHandle?.set("exact", exact)
                        popBackStack()
                    },
                    onBackClick = { popBackStack() }
                )
            }

            composable<Screen.DetalleServicio> { backStackEntry ->
                val serviceDetail: Screen.DetalleServicio = backStackEntry.toRoute()
                ServiceDetailScreen(
                    postId = serviceDetail.id,
                    onBackClick = { popBackStack() },
                    onViewProfileClick = { authorId ->
                        navigate(
                            Screen.PerfilProveedor(
                                providerId = authorId,
                                name = serviceDetail.providerName,
                                specialty = "Especialista",
                                rating = serviceDetail.rating,
                                reviewsCount = serviceDetail.reviewsCount,
                                location = serviceDetail.location,
                                imageRes = serviceDetail.providerImageRes
                            )
                        )
                    },
                    onSolicitarClick = { postId ->
                        navigate(
                            Screen.SolicitarServicio(
                                serviceId = postId,
                                serviceTitle = serviceDetail.title,
                                serviceCategory = serviceDetail.category,
                                servicePriceRange = serviceDetail.priceRange,
                                serviceImageRes = serviceDetail.imageRes,
                                providerId = serviceDetail.providerId,
                                providerName = serviceDetail.providerName,
                                providerSpecialty = serviceDetail.providerSpecialty,
                                providerImageRes = serviceDetail.providerImageRes,
                                location = serviceDetail.location
                            )
                        )
                    }
                )
            }

            composable<Screen.SolicitarServicio> { backStackEntry ->
                val solicitarData: Screen.SolicitarServicio = backStackEntry.toRoute()
                SolicitarServicioScreen(
                    data = solicitarData,
                    onBack = { popBackStack() },
                    onSendSuccess = {
                        navigate(Screen.Dashboard(successMessage = "Solicitud enviada exitosamente")) {
                            popUpTo<Screen.Dashboard> { inclusive = true }
                        }
                    }
                )
            }

            composable<Screen.MisSolicitudes> {
                MisSolicitudesScreen(
                    onBack = { popBackStack() }
                )
            }

            composable<Screen.MisTrabajos> {
                MisTrabajosScreen(
                    onBack = { popBackStack() }
                )
            }

            composable<Screen.PerfilProveedor> { backStackEntry ->
                val profileData: Screen.PerfilProveedor = backStackEntry.toRoute()
                ProviderProfileScreen(
                    profileData = profileData,
                    onBackClick = { popBackStack() },
                    onContactClick = { user ->
                        navigate(
                            Screen.Chat(
                                providerId = user.id,
                                providerName = user.fullName,
                                providerSpecialty = user.role.name,
                                providerImageRes = profileData.imageRes,
                                providerImageUrl = user.profilePictureUrl
                            )
                        )
                    }
                )
            }

            composable<Screen.Chat> { backStackEntry ->
                val chatData: Screen.Chat = backStackEntry.toRoute()
                ChatScreen(
                    chatData = chatData,
                    onBack = { popBackStack() }
                )
            }
        }
    }
}
