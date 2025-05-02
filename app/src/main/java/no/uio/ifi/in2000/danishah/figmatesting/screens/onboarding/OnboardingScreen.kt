package no.uio.ifi.in2000.danishah.figmatesting.screens.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sailing
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Velkommen til FiskeFinner!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "Fortell oss om dine foretrukne fiskeforhold, så kan vi gi deg bedre anbefalinger.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Din profil",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = { viewModel.updateName(it) },
                    label = { Text("Navn") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Navn") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }
        
        PreferenceCard(
            title = "Værforhold",
            description = "Hvor godt liker du å fiske under følgende værforhold?"
        ) {
            PreferenceSlider(
                icon = Icons.Default.Thermostat,
                label = "Temperatur",
                value = uiState.temperaturePreference,
                lowValueLabel = "Kaldere",
                highValueLabel = "Varmere",
                onValueChange = { viewModel.updateTemperaturePreference(it) }
            )
            
            PreferenceSlider(
                icon = Icons.Default.Air,
                label = "Vind",
                value = uiState.windPreference,
                lowValueLabel = "Vindstille",
                highValueLabel = "Mye vind",
                onValueChange = { viewModel.updateWindPreference(it) }
            )
            
            PreferenceSlider(
                icon = Icons.Default.Umbrella,
                label = "Nedbør",
                value = uiState.rainPreference,
                lowValueLabel = "Tørt",
                highValueLabel = "Regn",
                onValueChange = { viewModel.updateRainPreference(it) }
            )
            
            // Pressure preference
            PreferenceSlider(
                icon = Icons.Default.CompassCalibration,
                label = "Lufttrykk",
                value = uiState.pressurePreference,
                lowValueLabel = "Lavt",
                highValueLabel = "Høyt",
                onValueChange = { viewModel.updatePressurePreference(it) }
            )
            
            PreferenceSlider(
                icon = Icons.Default.CloudQueue,
                label = "Skydekke",
                value = uiState.cloudPreference,
                lowValueLabel = "Klart",
                highValueLabel = "Overskyet",
                onValueChange = { viewModel.updateCloudPreference(it) }
            )
        }
        
        PreferenceCard(
            title = "Tid på døgnet",
            description = "Når foretrekker du å fiske?"
        ) {
            PreferenceSlider(
                icon = Icons.Default.WbSunny,
                label = "Morgen",
                value = uiState.morningPreference,
                lowValueLabel = "Ikke foretrukket",
                highValueLabel = "Veldig foretrukket",
                onValueChange = { viewModel.updateMorningPreference(it) }
            )
            
            PreferenceSlider(
                icon = Icons.Default.BrightnessHigh,
                label = "Midt på dagen",
                value = uiState.afternoonPreference,
                lowValueLabel = "Ikke foretrukket",
                highValueLabel = "Veldig foretrukket",
                onValueChange = { viewModel.updateAfternoonPreference(it) }
            )
            
            PreferenceSlider(
                icon = Icons.Default.WbTwilight,
                label = "Kveld",
                value = uiState.eveningPreference,
                lowValueLabel = "Ikke foretrukket",
                highValueLabel = "Veldig foretrukket",
                onValueChange = { viewModel.updateEveningPreference(it) }
            )
        }
        
        PreferenceCard(
            title = "Årstider",
            description = "Hvilke årstider foretrekker du å fiske i?"
        ) {
            PreferenceSlider(
                icon = Icons.Default.Grass,
                label = "Vår",
                value = uiState.springPreference,
                lowValueLabel = "Ikke foretrukket",
                highValueLabel = "Veldig foretrukket",
                onValueChange = { viewModel.updateSpringPreference(it) }
            )
            
            PreferenceSlider(
                icon = Icons.Default.LightMode,
                label = "Sommer",
                value = uiState.summerPreference,
                lowValueLabel = "Ikke foretrukket",
                highValueLabel = "Veldig foretrukket",
                onValueChange = { viewModel.updateSummerPreference(it) }
            )
            
            PreferenceSlider(
                icon = Icons.Default.BrightnessLow,
                label = "Høst",
                value = uiState.fallPreference,
                lowValueLabel = "Ikke foretrukket",
                highValueLabel = "Veldig foretrukket",
                onValueChange = { viewModel.updateFallPreference(it) }
            )
            
            PreferenceSlider(
                icon = Icons.Default.AcUnit,
                label = "Vinter",
                value = uiState.winterPreference,
                lowValueLabel = "Ikke foretrukket",
                highValueLabel = "Veldig foretrukket",
                onValueChange = { viewModel.updateWinterPreference(it) }
            )
        }
        
        Button(
            onClick = {
                viewModel.savePreferences()
                onComplete()
            },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(vertical = 16.dp),
            enabled = uiState.name.isNotBlank()
        ) {
            Text("Start å fiske!", modifier = Modifier.padding(vertical = 8.dp))
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun PreferenceCard(
    title: String,
    description: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            content()
        }
    }
}

@Composable
fun PreferenceSlider(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: Int,
    lowValueLabel: String,
    highValueLabel: String,
    onValueChange: (Int) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(24.dp)
            )
            
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 8.dp)
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = "$value/5",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 1f..5f,
            steps = 3,
            modifier = Modifier.fillMaxWidth()
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = lowValueLabel,
                style = MaterialTheme.typography.bodySmall
            )
            
            Text(
                text = highValueLabel,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
} 