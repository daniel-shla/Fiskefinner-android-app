package no.uio.ifi.in2000.danishah.figmatesting.screens.map.mittFiske

import android.util.Log
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
import no.uio.ifi.in2000.danishah.figmatesting.ml.SpeciesMapper
import no.uio.ifi.in2000.danishah.figmatesting.screens.dashboard.LoactionForecast.WeatherViewModel
import no.uio.ifi.in2000.danishah.figmatesting.screens.dashboard.PredictionViewModel
import java.time.LocalDate
import java.time.LocalTime

// UI state for viewmodelen
data class MittFiskeUiState(
    val locations: List<MittFiskeLocation> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val selectedSpecies: String? = null
)

class MittFiskeViewModel(
    private val repository: MittFiskeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MittFiskeUiState())
    val uiState: StateFlow<MittFiskeUiState> = _uiState

    private val allLocations = mutableListOf<MittFiskeLocation>()

    // Henter lokasjoner og starter prediksjon
    fun loadLocations(
        polygonWKT: String,
        pointWKT: String,
        min: Int = 13,
        max: Int = 20,
        weatherViewModel: WeatherViewModel,
        predictionViewModel: PredictionViewModel,
        selectedSpecies: String,
        onDone: (() -> Unit)? = null
    ) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            repository.getLocationsForArea(polygonWKT, pointWKT, min, max)
                .onSuccess { locations ->
                    allLocations.clear()
                    allLocations.addAll(locations)

                    _uiState.update {
                        it.copy(
                            locations = locations,
                            isLoading = false,
                            selectedSpecies = selectedSpecies.trim().lowercase()
                        )
                    }

                    rateAllLocationsWithAI(weatherViewModel, predictionViewModel, selectedSpecies, onDone)

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

    // Filtrerer vekk lokasjoner utenfor kartets synlige område
    fun filterLocationsInBounds(
        locations: List<MittFiskeLocation>,
        bounds: CoordinateBounds
    ): List<MittFiskeLocation> {
        return locations.filter { loc ->
            val lat = loc.toPoint().latitude()
            val lng = loc.toPoint().longitude()
            lat in bounds.southwest.latitude()..bounds.northeast.latitude() &&
                    lng in bounds.southwest.longitude()..bounds.northeast.longitude()
        }
    }

    // Kjører prediksjon for alle lokasjoner og oppdaterer rating basert på predikert klasse
    fun rateAllLocationsWithAI(
        weatherViewModel: WeatherViewModel,
        predictionViewModel: PredictionViewModel,
        selectedSpecies: String,
        onDone: (() -> Unit)? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val speciesId = SpeciesMapper.getId(selectedSpecies)
            if (speciesId < 0) return@launch

            val selected = selectedSpecies.trim().lowercase()

            val relevantLocations = allLocations.toList().filter { loc ->
                val allFish = loc.locs.flatMap { it.fe ?: emptyList() }
                val species = extractSupportedFish(allFish)
                selected in species
            }


            val rated = relevantLocations.mapNotNull { loc ->
                try {
                    val weather = weatherViewModel.getWeatherForLocation(
                        lat = loc.p.coordinates[1],
                        lon = loc.p.coordinates[0]
                    )

                    val input = makeTrainingData(loc, weather, speciesId)

                    Log.d("AI_INPUT", "Species ID: $speciesId for ${loc.name}")
                    Log.d("AI_INPUT", "TrainingData: $input")

                    val predictedClass = withContext(Dispatchers.Default) {
                        predictionViewModel.predictFishingClass(input)
                    }

                    Log.d("AI_CLASS", "Class for ${loc.name}: $predictedClass")
                    Log.d("RATING_CHECK", "Rating for ${loc.name}: ${predictedClass + 1}")


                    loc.copy(rating = predictedClass + 1) // Rating mellom 1–4

                } catch (e: Exception) {
                    Log.e("AI_ERROR", "Feil ved ${loc.name}: ${e.message}")
                    null
                }
            }

            _uiState.update {
                it.copy(locations = rated, selectedSpecies = selected)
            }

            withContext(Dispatchers.Main) { onDone?.invoke() }
            allLocations.clear()
            allLocations.addAll(rated)

        }
    }

    // Lager inputdata for modellen basert på vær og lokasjon
    private fun makeTrainingData(
        loc: MittFiskeLocation,
        weather: TimeSeries,
        speciesId: Float
    ): TrainingData {
        val now = LocalDate.now()
        val hour = LocalTime.now().hour.toFloat()
        val details = weather.data.instant.details

        return TrainingData(
            speciesId = speciesId,
            temperature = details.air_temperature?.toFloat() ?: 0f,
            windSpeed = details.wind_speed?.toFloat() ?: 0f,
            precipitation = weather.data.next_1_hours?.details?.precipitation_amount?.toFloat() ?: 0f,
            airPressure = details.air_pressure_at_sea_level?.toFloat() ?: 0f,
            cloudCover = details.cloud_area_fraction?.toFloat() ?: 0f,
            timeOfDay = hour,
            season = when (now.monthValue) {
                in 3..5 -> 1f
                in 6..8 -> 2f
                in 9..11 -> 3f
                else -> 4f
            },
            latitude = loc.p.coordinates[1].toFloat(),
            longitude = loc.p.coordinates[0].toFloat(),
            fishCaught = 0
        )
    }

    // Fjerner fiskearter vi ikke støtter i modelltreninga
    private fun extractSupportedFish(fe: List<String>?): List<String> {
        val supported = listOf(
            "torsk", "makrell", "sei", "ørret", "sjøørret", "laks", "gjedde",
            "røye", "hyse", "abbor", "havabbor", "steinbit", "kveite", "rødspette"
        )

        return fe?.flatMap { entry ->
            entry.split(",")
                .map { it.trim().lowercase() }
                .filter { it in supported }
        } ?: emptyList()
    }

    // Returnerer lokasjoner filtrert på valgt art
    fun getLocationsForSelectedSpecies(): List<MittFiskeLocation> {
        val selected = _uiState.value.selectedSpecies ?: return allLocations
        return allLocations.filter { loc ->
            val allFe = loc.locs.flatMap { it.fe ?: emptyList() }
            val fish = extractSupportedFish(allFe)
            selected in fish
        }
    }

    // Setter valgt art eksplisitt
    fun selectSpecies(species: String) {
        _uiState.update { it.copy(selectedSpecies = species.trim().lowercase()) }
    }
}
