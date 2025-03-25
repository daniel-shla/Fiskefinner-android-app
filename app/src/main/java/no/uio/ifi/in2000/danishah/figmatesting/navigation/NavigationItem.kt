package no.uio.ifi.in2000.danishah.figmatesting.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector
import no.uio.ifi.in2000.danishah.figmatesting.R

sealed class NavigationItem(val route: String, val icon: ImageVector, val titleResId: Int) {
    data object Map : NavigationItem("map", Icons.Default.Map, R.string.nav_map)
    data object Dashboard : NavigationItem("dashboard", Icons.Default.Dashboard, R.string.nav_dashboard)
    data object Profile : NavigationItem("profile", Icons.Default.Person, R.string.nav_profile)
    data object FishSelection : NavigationItem("fish_selection", Icons.Default.WaterDrop, R.string.nav_fish_types)
} 