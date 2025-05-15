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
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.danishah.figmatesting.screens.dashboard.LoactionForecast.WeatherViewModel
import no.uio.ifi.in2000.danishah.figmatesting.screens.dashboard.PredictionViewModel
import no.uio.ifi.in2000.danishah.figmatesting.screens.map.mittFiske.MittFiskeViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    mittFiskeViewModel: MittFiskeViewModel,
    weatherViewModel: WeatherViewModel,
    predictionViewModel: PredictionViewModel,
    onComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        val polygonWKT = "POLYGON((4.0 71.5, 4.0 57.9, 31.5 57.9, 31.5 71.5, 4.0 71.5))"
        val pointWKT = "POINT(15.0 64.0)"
        viewModel.preloadFishLocations(
            mittFiskeViewModel, weatherViewModel, predictionViewModel,
            polygonWKT, pointWKT, "ørret", onDone = {}
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
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
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,

                )
            Text(
                text = "Fortell oss om dine foretrukne fiskeforhold, så kan vi gi deg bedre anbefalinger.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 8.dp),
                color = MaterialTheme.colorScheme.onBackground

            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Din profil", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
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

            PreferenceCard("Værforhold", "Hvor godt liker du å fiske under følgende værforhold?") {
                PreferenceSlider(Icons.Default.Thermostat, "Temperatur", uiState.temperaturePreference, "Kaldere", "Varmere") {
                    viewModel.updateTemperaturePreference(it)
                }
                PreferenceSlider(Icons.Default.Air, "Vind", uiState.windPreference, "Vindstille", "Mye vind") {
                    viewModel.updateWindPreference(it)
                }
                PreferenceSlider(Icons.Default.Umbrella, "Nedbør", uiState.rainPreference, "Tørt", "Regn") {
                    viewModel.updateRainPreference(it)
                }
                PreferenceSlider(Icons.Default.CompassCalibration, "Lufttrykk", uiState.pressurePreference, "Lavt", "Høyt") {
                    viewModel.updatePressurePreference(it)
                }
                PreferenceSlider(Icons.Default.CloudQueue, "Skydekke", uiState.cloudPreference, "Klart", "Overskyet") {
                    viewModel.updateCloudPreference(it)
                }
            }

            PreferenceCard("Tid på døgnet", "Når foretrekker du å fiske?") {
                PreferenceSlider(Icons.Default.WbSunny, "Morgen", uiState.morningPreference, "Ikke foretrukket", "Veldig foretrukket") {
                    viewModel.updateMorningPreference(it)
                }
                PreferenceSlider(Icons.Default.BrightnessHigh, "Midt på dagen", uiState.afternoonPreference, "Ikke foretrukket", "Veldig foretrukket") {
                    viewModel.updateAfternoonPreference(it)
                }
                PreferenceSlider(Icons.Default.WbTwilight, "Kveld", uiState.eveningPreference, "Ikke foretrukket", "Veldig foretrukket") {
                    viewModel.updateEveningPreference(it)
                }
            }

            PreferenceCard("Årstider", "Hvilke årstider foretrekker du å fiske i?") {
                PreferenceSlider(Icons.Default.Grass, "Vår", uiState.springPreference, "Ikke foretrukket", "Veldig foretrukket") {
                    viewModel.updateSpringPreference(it)
                }
                PreferenceSlider(Icons.Default.LightMode, "Sommer", uiState.summerPreference, "Ikke foretrukket", "Veldig foretrukket") {
                    viewModel.updateSummerPreference(it)
                }
                PreferenceSlider(Icons.Default.BrightnessLow, "Høst", uiState.fallPreference, "Ikke foretrukket", "Veldig foretrukket") {
                    viewModel.updateFallPreference(it)
                }
                PreferenceSlider(Icons.Default.AcUnit, "Vinter", uiState.winterPreference, "Ikke foretrukket", "Veldig foretrukket") {
                    viewModel.updateWinterPreference(it)
                }
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
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(description, style = MaterialTheme.typography.bodySmall)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
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
            Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(24.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(start = 8.dp)
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
            Text(lowValueLabel, style = MaterialTheme.typography.bodySmall)
            Text(highValueLabel, style = MaterialTheme.typography.bodySmall)
        }
    }
}

