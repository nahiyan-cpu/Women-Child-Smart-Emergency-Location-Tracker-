package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.aisafety.AiSafetyScreen
import com.example.ui.aisafety.AiSafetyViewModel
import com.example.ui.contacts.ContactsScreen
import com.example.ui.contacts.ContactsViewModel
import com.example.ui.dangerzones.DangerZonesScreen
import com.example.ui.dangerzones.DangerZonesViewModel
import com.example.ui.history.HistoryViewModel
import com.example.ui.history.IncidentHistoryScreen
import com.example.ui.home.HomeScreen
import com.example.ui.home.HomeViewModel
import com.example.ui.profile.ProfileScreen
import com.example.ui.profile.ProfileViewModel
import com.example.ui.safeplaces.SafePlacesScreen
import com.example.ui.safeplaces.SafePlacesViewModel
import com.example.ui.sos.SosActiveScreen
import com.example.ui.sos.SosViewModel

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Sos : Screen("sos")
    data object Contacts : Screen("contacts")
    data object SafePlaces : Screen("safe_places")
    data object DangerZones : Screen("danger_zones")
    data object AiSafety : Screen("ai_safety")
    data object History : Screen("history")
    data object Profile : Screen("profile")
}

@Composable
fun AppNavigation(onSignOut: () -> Unit) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            val viewModel: HomeViewModel = viewModel()
            HomeScreen(
                viewModel = viewModel,
                onNavigateSos = { navController.navigate(Screen.Sos.route) },
                onNavigateContacts = { navController.navigate(Screen.Contacts.route) },
                onNavigateSafePlaces = { navController.navigate(Screen.SafePlaces.route) },
                onNavigateDangerZones = { navController.navigate(Screen.DangerZones.route) },
                onNavigateAiSafety = { navController.navigate(Screen.AiSafety.route) },
                onNavigateHistory = { navController.navigate(Screen.History.route) },
                onNavigateProfile = { navController.navigate(Screen.Profile.route) },
                onSignOut = onSignOut
            )
        }
        composable(Screen.Sos.route) {
            val viewModel: SosViewModel = viewModel()
            SosActiveScreen(
                viewModel = viewModel,
                onDismiss = { navController.popBackStack() }
            )
        }
        composable(Screen.Contacts.route) {
            val viewModel: ContactsViewModel = viewModel()
            ContactsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.SafePlaces.route) {
            val viewModel: SafePlacesViewModel = viewModel()
            SafePlacesScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.DangerZones.route) {
            val viewModel: DangerZonesViewModel = viewModel()
            DangerZonesScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.AiSafety.route) {
            val viewModel: AiSafetyViewModel = viewModel()
            AiSafetyScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.History.route) {
            val viewModel: HistoryViewModel = viewModel()
            IncidentHistoryScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Profile.route) {
            val viewModel: ProfileViewModel = viewModel()
            ProfileScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
