package no.uio.ifi.in2000.danishah.figmatesting.screens.map.mittFiske

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mapbox.maps.CoordinateBounds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.MittFiskeLocation
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.TimeSeries
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.TrainingData
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.toPoint
import no.uio.ifi.in2000.danishah.figmatesting.data.repository.MittFiskeRepository
import no.uio.ifi.in2000.danishah.figmatesting.screens.dashboard.LoactionForecast.WeatherViewModel
import no.uio.ifi.in2000.danishah.figmatesting.screens.dashboard.PredictionViewModel
import java.time.LocalDate
import java.time.LocalTime

data class MittFiskeUiState(
    val locations: List<MittFiskeLocation> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class MittFiskeViewModel(
    private val repository: MittFiskeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MittFiskeUiState())
    val uiState: StateFlow<MittFiskeUiState> = _uiState

    fun loadLocations(
        polygonWKT: String,
        pointWKT: String,
        min: Int = 13,
        max: Int = 20

    ) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            repository.getLocationsForArea(polygonWKT, pointWKT, min, max)
                .onSuccess { locations ->
                    _uiState.update {
                        it.copy(
                            locations = locations,
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "Ukjent feil"
                        )
                    }
                }
        }
    }

    fun filterLocationsInBounds(
        locations: List<MittFiskeLocation>, // eller hva slags type du bruker
        bounds: CoordinateBounds
    ): List<MittFiskeLocation> {
        return locations.filter { loc ->
            val lat = loc.toPoint().latitude()
            val lng = loc.toPoint().longitude()
            lat in bounds.southwest.latitude()..bounds.northeast.latitude() &&
                    lng in bounds.southwest.longitude()..bounds.northeast.longitude()
        }
    }

    fun rateAllLocationsWithAI(
        weatherViewModel: WeatherViewModel,
        predictionViewModel: PredictionViewModel
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = _uiState.value.locations.mapNotNull { loc ->
                try {
                    val weather = weatherViewModel.getWeatherForLocation(
                        lat = loc.p.coordinates[1],
                        lon = loc.p.coordinates[0]
                    )

                    val input = makeTrainingData(loc, weather)
                    val score = withContext(Dispatchers.Default) {
                        predictionViewModel.predictFishingSpot(input)
                    }

                    loc.copy(rating = (score * 5).toInt().coerceIn(1, 5))
                } catch (e: Exception) {
                    loc // fallback
                }
            }

            // Oppdater UI på hovedtråd
            _uiState.update { it.copy(locations = updated) }
        }
    }

    private fun makeTrainingData(loc: MittFiskeLocation, weather: TimeSeries): TrainingData {
        val now = LocalDate.now()
        val hour = LocalTime.now().hour.toFloat()
        val details = weather.data.instant.details

        return TrainingData(
            temperature = details.air_temperature.toFloat(),
            windSpeed = details.wind_speed.toFloat(),
            precipitation = weather.data.next_1_hours?.details?.precipitation_amount?.toFloat() ?: 0f,
            airPressure = details.air_pressure_at_sea_level.toFloat(),
            cloudCover = details.cloud_area_fraction.toFloat(),
            timeOfDay = hour,
            season = when (now.monthValue) {
                in 3..5 -> 1f // vår
                in 6..8 -> 2f // sommer
                in 9..11 -> 3f // høst
                else -> 4f // vinter
            },
            latitude = loc.p.coordinates[1].toFloat(),
            longitude = loc.p.coordinates[0].toFloat(),
            fishCaught = 0 // dummy
        )
    }
}
