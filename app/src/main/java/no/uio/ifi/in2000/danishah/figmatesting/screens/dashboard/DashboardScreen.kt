package no.uio.ifi.in2000.danishah.figmatesting.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import no.uio.ifi.in2000.danishah.figmatesting.screens.dashboard.cards.Catch
import no.uio.ifi.in2000.danishah.figmatesting.screens.dashboard.cards.CatchCard
import no.uio.ifi.in2000.danishah.figmatesting.screens.dashboard.cards.FishingSpot
import no.uio.ifi.in2000.danishah.figmatesting.screens.dashboard.cards.SpotCard
import no.uio.ifi.in2000.danishah.figmatesting.screens.dashboard.cards.WeatherCard

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory)) {
    val weatherData by viewModel.weatherData.collectAsState()
    val scrollState = rememberScrollState()
    
    // Sample spots data (In real app, this would come from ViewModel)
    val nearbySpots = listOf(
        FishingSpot(
            "Oslofjorden", 
            "15 min unna", 
            "Saltvann"
        ),
        FishingSpot(
            "Sognsvann", 
            "25 min unna", 
            "Ferskvann"
        ),
        FishingSpot(
            "Akerselva", 
            "10 min unna", 
            "Ferskvann"
        ),
        FishingSpot(
            "Lysakerelva", 
            "30 min unna", 
            "Ferskvann"
        )
    )
    
    // Sample recent catches (In real app, this would come from ViewModel)
    val recentCatches = listOf(
        Catch(
            "Ørret", 
            "3.5kg", 
            "Oslofjorden", 
            "2 dager siden"
        ),
        Catch(
            "Laks", 
            "2.1kg", 
            "Akerselva", 
            "1 uke siden"
        ),
        Catch(
            "Gjedde", 
            "5.2kg", 
            "Sognsvann", 
            "2 uker siden"
        )
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Welcome
        Text(
            text = "Velkommen tilbake!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Her er dagens fiskevarsling",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Weather card
        WeatherCard(weatherData)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Nearby fishing spots
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Fiskeplasser i nærheten",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Vis alle",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
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
                    text = "Nylige fangster",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Vis alle",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            recentCatches.forEach { catch ->
                CatchCard(catch)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

