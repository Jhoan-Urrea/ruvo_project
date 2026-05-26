package com.example.ruvo_app.core.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
import com.example.ruvo_app.features.settings.*
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
import com.example.ruvo_app.features.search.SearchScreen

@Composable
fun AppNavGraph(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val authState by authViewModel.uiState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthUiState.Unauthenticated) {
            navController.navigate(Screen.Home) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    val startDestination: Screen = when (authState) {
        is AuthUiState.Authenticated -> Screen.Dashboard()
        else -> Screen.Home
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.fillMaxSize()
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
            val loginViewModel: com.example.ruvo_app.features.login.LoginViewModel = hiltViewModel()
            LoginScreen(
                onBackClick = { navController.popBackStack() },
                onForgotPasswordClick = { navController.navigate(Screen.ForgotPassword) },
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard()) {
                        popUpTo(Screen.Home) { inclusive = true }
                    }
                },
                viewModel = loginViewModel
            )
        }

        composable<Screen.ForgotPassword> {
            val forgotPasswordViewModel: com.example.ruvo_app.features.login.ForgotPasswordViewModel = hiltViewModel()
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
                    navController.navigate(Screen.Dashboard()) {
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
                onSettingsClick = { navController.navigate(Screen.Settings) },
                onAddPostClick = { navController.navigate(Screen.CrearServicio) },
                onAdminDetailedClick = { navController.navigate(Screen.ModeratorDashboard) },
                onServiceClick = { service -> navController.navigate(service) },
                onChatListClick = { navController.navigate(Screen.ChatList) },
                onSolicitudesClick = { navController.navigate(Screen.MisSolicitudes) },
                onMisTrabajosClick = { navController.navigate(Screen.MisTrabajos) },
                isAdmin = state?.user?.role == UserRole.MODERATOR,
                initialSuccessMessage = dashboardData.successMessage,
                viewModel = dashboardViewModel
            )
        }

        composable<Screen.Search> {
            SearchScreen(
                onServiceClick = { serviceDetail -> navController.navigate(serviceDetail) }
            )
        }

        composable<Screen.ChatList> {
            ChatListScreen(
                onBackClick = { navController.popBackStack() },
                onChatClick = { chatId, userName, role, imageRes, imageUrl: String? ->
                    navController.navigate(
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
                onBack = { navController.popBackStack() }
            )
        }

        composable<Screen.Settings> {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onLogoutClick = { authViewModel.logout() },
                onEditProfileClick = { navController.navigate(Screen.EditProfile) },
                onSecurityClick = { navController.navigate(Screen.SecuritySettings) },
                onHelpCenterClick = { navController.navigate(Screen.HelpCenter) },
                onPrivacyTermsClick = { navController.navigate(Screen.PrivacyTerms) }
            )
        }

        composable<Screen.SecuritySettings> {
            SecuritySettingsScreen(
                onBackClick = { navController.popBackStack() },
                onChangePasswordClick = { navController.navigate(Screen.ForgotPassword) }
            )
        }

        composable<Screen.HelpCenter> {
            HelpCenterScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<Screen.PrivacyTerms> {
            PrivacyTermsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<Screen.Notifications> {
            NotificationsScreen()
        }

        composable<Screen.EditProfile> {
            val editViewModel: EditProfileViewModel = hiltViewModel()
            EditProfileScreen(
                onBackClick = { navController.popBackStack() },
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
                onBackClick = { navController.popBackStack() },
                onSelectLocationClick = { navController.navigate(Screen.SelectLocation) },
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
                    navController.previousBackStackEntry?.savedStateHandle?.set("lat", lat)
                    navController.previousBackStackEntry?.savedStateHandle?.set("lng", lng)
                    navController.previousBackStackEntry?.savedStateHandle?.set("address", full)
                    navController.previousBackStackEntry?.savedStateHandle?.set("country", country)
                    navController.previousBackStackEntry?.savedStateHandle?.set("region", region)
                    navController.previousBackStackEntry?.savedStateHandle?.set("city", city)
                    navController.previousBackStackEntry?.savedStateHandle?.set("exact", exact)
                    navController.popBackStack()
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<Screen.DetalleServicio> { backStackEntry ->
            val serviceDetail: Screen.DetalleServicio = backStackEntry.toRoute()
            ServiceDetailScreen(
                postId = serviceDetail.id,
                onBackClick = { navController.popBackStack() },
                onViewProfileClick = { authorId ->
                    navController.navigate(
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
                    navController.navigate(
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
                onBack = { navController.popBackStack() },
                onSendSuccess = {
                    navController.navigate(Screen.Dashboard(successMessage = "Solicitud enviada exitosamente")) {
                        popUpTo<Screen.Dashboard> { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.MisSolicitudes> {
            MisSolicitudesScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable<Screen.MisTrabajos> {
            MisTrabajosScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable<Screen.PerfilProveedor> { backStackEntry ->
            val profileData: Screen.PerfilProveedor = backStackEntry.toRoute()
            ProviderProfileScreen(
                profileData = profileData,
                onBackClick = { navController.popBackStack() },
                onContactClick = { user ->
                    navController.navigate(
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
                onBack = { navController.popBackStack() }
            )
        }
    }
}
