package no.uio.ifi.in2000.danishah.figmatesting

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import no.uio.ifi.in2000.danishah.figmatesting.navigation.NavigationItem
import no.uio.ifi.in2000.danishah.figmatesting.screens.dashboard.DashboardScreen
import no.uio.ifi.in2000.danishah.figmatesting.screens.dashboard.DashboardViewModel
import no.uio.ifi.in2000.danishah.figmatesting.screens.map.MapScreen
import no.uio.ifi.in2000.danishah.figmatesting.screens.map.MapViewModel
import no.uio.ifi.in2000.danishah.figmatesting.screens.profile.ProfileScreen
import no.uio.ifi.in2000.danishah.figmatesting.screens.profile.ProfileViewModel
import no.uio.ifi.in2000.danishah.figmatesting.ui.theme.FigmaTestingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FigmaTestingTheme {
                FishingApp()
            }
        }
    }
}

@Composable
fun FishingApp() {
    val navController = rememberNavController()
    val items = listOf(
        NavigationItem.Map,
        NavigationItem.Dashboard,
        NavigationItem.Profile
    )
    
    // Create ViewModels
    val mapViewModel: MapViewModel = viewModel(factory = MapViewModel.Factory)
    val dashboardViewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory)
    val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory)
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(text = item.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavigationItem.Map.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavigationItem.Map.route) {
                MapScreen(viewModel = mapViewModel)
            }
            composable(NavigationItem.Dashboard.route) {
                DashboardScreen(viewModel = dashboardViewModel)
            }
            composable(NavigationItem.Profile.route) {
                ProfileScreen(viewModel = profileViewModel)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FishingAppPreview() {
    FigmaTestingTheme {
        FishingApp()
    }
}