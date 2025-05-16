package no.uio.ifi.in2000.danishah.figmatesting.screens.profile

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateToPreferences: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val userPreferences by viewModel.userPreferences.collectAsState()
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(bottom = 8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profilbilde",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }
                
                Text(
                    text = userProfile?.name ?: "Bruker",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        var selectedTabIndex by remember { mutableIntStateOf(0) }
        val tabs = listOf("Værforhold", "Tid", "Årstider")
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Dine Preferanser",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                
                TabRow(selectedTabIndex = selectedTabIndex) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                when (selectedTabIndex) {
                    0 -> {
                        PreferenceProgress(
                            icon = Icons.Default.Thermostat,
                            label = "Temperatur",
                            value = userPreferences.temperaturePreference
                        )
                        
                        PreferenceProgress(
                            icon = Icons.Default.Air,
                            label = "Vind",
                            value = userPreferences.windPreference
                        )
                        
                        PreferenceProgress(
                            icon = Icons.Default.Umbrella,
                            label = "Nedbør",
                            value = userPreferences.rainPreference
                        )
                        
                        PreferenceProgress(
                            icon = Icons.Default.CompassCalibration,
                            label = "Lufttrykk",
                            value = userPreferences.pressurePreference
                        )
                        
                        PreferenceProgress(
                            icon = Icons.Default.CloudQueue,
                            label = "Skydekke",
                            value = userPreferences.cloudPreference
                        )
                    }
                    
                    1 -> {
                        PreferenceProgress(
                            icon = Icons.Default.WbSunny,
                            label = "Morgen",
                            value = userPreferences.morningPreference
                        )
                        
                        PreferenceProgress(
                            icon = Icons.Default.BrightnessHigh,
                            label = "Midt på dagen",
                            value = userPreferences.afternoonPreference
                        )
                        
                        PreferenceProgress(
                            icon = Icons.Default.WbTwilight,
                            label = "Kveld",
                            value = userPreferences.eveningPreference
                        )
                    }
                    
                    2 -> {
                        PreferenceProgress(
                            icon = Icons.Default.Grass,
                            label = "Vår",
                            value = userPreferences.springPreference
                        )
                        
                        PreferenceProgress(
                            icon = Icons.Default.LightMode,
                            label = "Sommer",
                            value = userPreferences.summerPreference
                        )
                        
                        PreferenceProgress(
                            icon = Icons.Default.BrightnessLow,
                            label = "Høst",
                            value = userPreferences.fallPreference
                        )
                        
                        PreferenceProgress(
                            icon = Icons.Default.AcUnit,
                            label = "Vinter",
                            value = userPreferences.winterPreference
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        ElevatedButton(
            onClick = { onNavigateToPreferences() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Endre preferanser")
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        ElevatedButton(
            onClick = {
                scope.launch {
                    val success = viewModel.logout()
                    if (success) {
                        onLogout()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Logg ut")
        }
    }
}

@Composable
fun PreferenceProgress(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: Int
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = "$value/5",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = { value/5f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        )
    }
} 