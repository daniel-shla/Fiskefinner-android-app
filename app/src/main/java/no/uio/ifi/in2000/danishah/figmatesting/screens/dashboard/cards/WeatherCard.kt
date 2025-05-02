package no.uio.ifi.in2000.danishah.figmatesting.screens.dashboard.cards

import TimeSeries
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.TrainingData
import no.uio.ifi.in2000.danishah.figmatesting.screens.dashboard.LoactionForecast.WeatherViewModel
import no.uio.ifi.in2000.danishah.figmatesting.screens.dashboard.PredictionViewModel
import java.time.LocalDate
import java.time.LocalTime

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeatherCard(weatherData: TimeSeries?) {
    val weatherViewModel: WeatherViewModel = viewModel()
    val predictionViewModel: PredictionViewModel = viewModel()

    // hente værdata fra locationforecast for prediction
    val uiState by weatherViewModel.uiState.collectAsState()

    // vent mens værdata lastes inn
    val (temperature, windSpeed, precipitation) = when (uiState) {
        is WeatherUiState.Success -> weatherViewModel.getWeatherForPrediction()
        else -> Triple(0.0, 0.0, 0.0) // standardverdier mens vi laster
    }

    // start prediksjon
    LaunchedEffect(weatherData) {
        val details = weatherData?.data?.instant?.details

        if (details != null) {
            val trainingInput = TrainingData(
                temperature = details.air_temperature.toFloat(),
                windSpeed = details.wind_speed.toFloat(),
                precipitation = weatherData.data.next_1_hours?.details?.precipitation_amount?.toFloat() ?: 0f,
                airPressure = details.air_pressure_at_sea_level.toFloat(),
                cloudCover = details.cloud_area_fraction.toFloat(),

                // utlede ELLER hardkode (:
                timeOfDay = LocalTime.now().hour.toFloat(), // f.eks. 13.0
                season = when (LocalDate.now().monthValue) {
                    in 3..5 -> 1f // vår
                    in 6..8 -> 2f //sommer
                    in 9..11 -> 3f
                    else -> 4f },
                // season = getSeason(LocalDate.now().monthValue), // SLETTET funksjon nedenfor
                latitude = 59.9f, // Oslo-ish
                longitude = 10.75f,
                fishCaught = 0 // dummy, ikke brukt i prediction
            )

            predictionViewModel.predictFishingConditions(trainingInput)
        }
    }

    val predictionText by predictionViewModel.predictionText.collectAsState()

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
                    text = "Dagens vær",
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
                Icon(
                    imageVector = Icons.Outlined.WbSunny,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "${weatherData?.data?.instant?.details?.air_temperature?.toInt() ?: 0}°C",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Solrikt",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Column(horizontalAlignment = Alignment.End) {
                    WeatherDetail(
                        icon = Icons.Outlined.Air,
                        value = "${weatherData?.data?.instant?.details?.wind_speed ?: 5} m/s",
                        label = "Vind"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    WeatherDetail(
                        icon = Icons.Default.Cloud,
                        value = "${weatherData?.data?.instant?.details?.cloud_area_fraction ?: 50} %",
                        label = "Skydekke"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                        text = predictionText, // vise prediction text <3
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherDetail(
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