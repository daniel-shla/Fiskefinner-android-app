package no.uio.ifi.in2000.danishah.figmatesting.screens.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import no.uio.ifi.in2000.danishah.figmatesting.R

/**
 * Composable function for the Dashboard screen
 */
@Composable
fun DashboardScreen(viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory)) {
    // Collect state from ViewModel
    val weatherData by viewModel.weatherData.collectAsState()
    val scrollState = rememberScrollState()
    
    // Sample spots data (In real app, this would come from ViewModel)
    val nearbySpots = listOf(
        FishingSpot("Oslofjord", "15 min away", "Saltwater"),
        FishingSpot("Lake Sognsvann", "25 min away", "Freshwater"),
        FishingSpot("Akerselva", "10 min away", "Freshwater"),
        FishingSpot("Lysaker River", "30 min away", "Freshwater")
    )
    
    // Sample recent catches (In real app, this would come from ViewModel)
    val recentCatches = listOf(
        Catch("Trout", "3.5kg", "Oslofjord", "2 days ago"),
        Catch("Salmon", "2.1kg", "Akerselva", "1 week ago"),
        Catch("Pike", "5.2kg", "Lake Sognsvann", "2 weeks ago")
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Welcome section
        Text(
            text = "Welcome back!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Here's today's fishing forecast",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Weather card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Weather",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text(
                            text = "Oslo",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Weather icon
                    Icon(
                        imageVector = Icons.Outlined.WbSunny,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Temperature and condition
                    Column {
                        Text(
                            text = "${weatherData?.temperature?.toInt() ?: 0}°C",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = weatherData?.condition ?: "Sunny",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Additional weather info
                    Column(horizontalAlignment = Alignment.End) {
                        WeatherDetail(
                            icon = Icons.Outlined.Air,
                            value = "${weatherData?.windSpeed ?: 5} m/s",
                            label = "Wind"
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        WeatherDetail(
                            icon = Icons.Default.WaterDrop,
                            value = "65%",
                            label = "Humidity"
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Fishing quality indicator
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Good day for fishing! 👍",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Nearby fishing spots
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Nearby Fishing Spots",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "View All",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(nearbySpots) { spot ->
                    SpotCard(spot)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Recent catches
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Catches",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "View All",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            recentCatches.forEach { catch ->
                CatchItem(catch)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun WeatherDetail(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SpotCard(spot: FishingSpot) {
    Card(
        modifier = Modifier.width(160.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Image placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center)
                )
            }
            
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = spot.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = spot.distance,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = spot.type,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun CatchItem(catch: Catch) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Fish icon placeholder
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = catch.fishType,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = catch.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = catch.weight,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = catch.time,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

data class FishingSpot(
    val name: String,
    val distance: String,
    val type: String
)

data class Catch(
    val fishType: String,
    val weight: String,
    val location: String,
    val time: String
) 