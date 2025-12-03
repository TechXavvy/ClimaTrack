package ph.edu.auf.xavier.ardillo.climatrack.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation(apiKey: String) {
    val navController = rememberNavController()

    // No Scaffold / no bottom bar; a clean full-screen host
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = Modifier
    ) {
        composable(Screen.Home.route) { HomeScreen(apiKey) }
        composable(Screen.Checklist.route) { CheckListScreen() }
        composable(Screen.Settings.route) { SettingsScreen() }
    }
}
