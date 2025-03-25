package no.uio.ifi.in2000.danishah.figmatesting.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector


sealed class NavigationItem(val route: String, val icon: ImageVector, val title: String) {
    data object Map : NavigationItem("map", Icons.Default.Map, "Map")
    data object Dashboard : NavigationItem("dashboard", Icons.Default.Dashboard, "Dashboard")
    data object Profile : NavigationItem("profile", Icons.Default.Person, "Profile")
    data object FishSelection : NavigationItem("fish_selection", Icons.Default.WaterDrop, "Fish Types")
} 