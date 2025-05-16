package no.uio.ifi.in2000.danishah.figmatesting.screens.fishselection

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.FishSpeciesData
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.RatedPolygon
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.TimeSeries
import no.uio.ifi.in2000.danishah.figmatesting.data.repository.FishSpeciesRepository
import no.uio.ifi.in2000.danishah.figmatesting.ml.FishPredictor
import no.uio.ifi.in2000.danishah.figmatesting.screens.dashboard.LoactionForecast.WeatherViewModel


class FishSpeciesViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = FishSpeciesRepository(application.applicationContext)
    
    private val _availableSpecies = MutableStateFlow<List<FishSpeciesData>>(emptyList())
    
    private val _speciesStates = MutableStateFlow<Map<String, SpeciesState>>(emptyMap())
    val speciesStates: StateFlow<Map<String, SpeciesState>> = _speciesStates.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val maxConcurrentSpecies = 8

    private val weatherCache = mutableMapOf<Pair<Int, Int>, TimeSeries>()


    init {
        loadAvailableSpecies()
    }

    private fun loadAvailableSpecies() {
        val species = repository.getAvailableFishSpecies()
        _availableSpecies.value = species
        
        val initialStates = species.associate {
            it.scientificName to SpeciesState(
                species = it,
                isEnabled = false,
                isLoaded = false
            )
        }
        _speciesStates.value = initialStates
    }
    
    fun toggleSpecies(scientificName: String, weatherViewModel: WeatherViewModel) {
        val currentStates = _speciesStates.value.toMutableMap()
        val currentState = currentStates[scientificName] ?: return
        
        val newEnabled = !currentState.isEnabled
        
        if (newEnabled) {
            val currentlyEnabled = currentStates.values.count { it.isEnabled }
            if (currentlyEnabled >= maxConcurrentSpecies) {
                _errorMessage.value = "Maksimalt $maxConcurrentSpecies arter kan vises samtidig."
                return
            }
        }
        
        viewModelScope.launch {
            // If enabling and not loaded yet, load the data
            if (newEnabled && !currentState.isLoaded) {
                _isLoading.value = true
                _errorMessage.value = null
                
                try {
                    val fishData = repository.loadFishSpeciesPolygons(scientificName)
                    
                    if (fishData != null) {
                        // Update with loaded data
                        val ratedData = rateOneSpecies(fishData, weatherViewModel)
                        currentStates[scientificName] = SpeciesState(
                            species = ratedData,
                            isEnabled = true,
                            isLoaded = true
                        )
                    } else {
                        currentStates[scientificName] = currentState.copy(
                            isEnabled = true
                        )
                        _errorMessage.value = "Kunne ikke laste data for ${FishSpeciesData.getCommonName(scientificName)}"
                    }
                } catch (e: Exception) {
                    // Just toggle the state if loading fails
                    currentStates[scientificName] = currentState.copy(
                        isEnabled = true
                    )
                    _errorMessage.value = "Feil ved lasting av data: ${e.message}"
                } finally {
                    _isLoading.value = false
                }
            } else {
                currentStates[scientificName] = currentState.copy(
                    isEnabled = newEnabled
                )
            }
            
            _speciesStates.value = currentStates
        }
    }



    

    data class SpeciesState(
        val species: FishSpeciesData,
        val isEnabled: Boolean,
        val isLoaded: Boolean,
        val opacity: Float = 1.0f  // Default full opacity
    )
    

    fun updateSpeciesOpacity(scientificName: String, opacity: Float) {
        val currentStates = _speciesStates.value.toMutableMap()
        val currentState = currentStates[scientificName] ?: return
        
        currentStates[scientificName] = currentState.copy(opacity = opacity)
        _speciesStates.value = currentStates
    }


    private suspend fun rateOneSpecies(
        species: FishSpeciesData,
        weatherViewModel: WeatherViewModel
    ): FishSpeciesData {
        val predictor = FishPredictor(getApplication())
        var teller = 0
        val rated = species.polygons.mapNotNull { polygon ->
            if (polygon.isEmpty()) {
                Log.d("FishRating", " Tom polygon, hopper over")
                return@mapNotNull null
            }

            val (lon, lat) = getCentroid(polygon)

            val key = coordinateKey(lat, lon)
            val weather = weatherCache.getOrPut(key) {
                try {
                    weatherViewModel.getWeatherForLocation(lat, lon)
                } catch (e: Exception) {
                    null
                }!!
            }


            val input = floatArrayOf(
                weather.data.instant.details.air_temperature.toFloat(),
                weather.data.instant.details.wind_speed.toFloat(),
                weather.data.instant.details.cloud_area_fraction.toFloat(),
                weather.data.next_1_hours?.details?.precipitation_amount?.toFloat() ?: 0f,
                lat.toFloat(),
                lon.toFloat(),
                0f, 0f, 0f, 0f
            )

            val rating = predictor.predict(input)
            teller+=1
            if (teller % 100 == 0) {
                Log.d("RATING", "Rating polygon #$teller: $rating")
            }
            //Log.d("FishRating", " Rated polygon ($lat, $lon): $rating $teller")

            RatedPolygon(points = polygon, rating = rating)
        }

        return species.copy(ratedPolygons = rated)
    }






    private fun getCentroid(polygon: List<Pair<Double, Double>>): Pair<Double, Double> {
        val (sumLon, sumLat) = polygon.fold(0.0 to 0.0) { acc, coord ->
            acc.first + coord.first to acc.second + coord.second
        }
        val count = polygon.size
        return (sumLon / count) to (sumLat / count)
    }

    private fun coordinateKey(lat: Double, lon: Double, precision: Double = 0.15): Pair<Int, Int> {
        val factor = 1 / precision
        return (lat * factor).toInt() to (lon * factor).toInt()
    }




    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FishSpeciesViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return FishSpeciesViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 