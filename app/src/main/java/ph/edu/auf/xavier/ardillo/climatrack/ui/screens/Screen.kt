package ph.edu.auf.xavier.ardillo.climatrack.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Checklist : Screen("checklist", "Checklist", Icons.Default.CheckCircle)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}