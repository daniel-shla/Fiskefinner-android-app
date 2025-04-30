package no.uio.ifi.in2000.danishah.figmatesting.screens.dashboard

import TimeSeries
import WeatherUiState
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.danishah.figmatesting.data.repository.WeatherRepository

class DashboardViewModel : ViewModel() {
    // Weather data state
    private val _weatherData = MutableStateFlow<WeatherData?>(null)
    val weatherData: StateFlow<WeatherData?> = _weatherData.asStateFlow()

    private val repository: WeatherRepository = WeatherRepository()
    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState

    init {
        // Initialize weather data
        loadWeatherData()
    }
    
    private fun loadWeatherData() {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            try {
                val data = repository.getWeather(59.9139, 10.7522)
                _uiState.value = WeatherUiState.Success(data)
            } catch (e: Exception) {
                _uiState.value = WeatherUiState.Error("Feil ved henting av værdata: ${e.message}")
            }
        }
    }

    fun getCurrentWeather(): List<TimeSeries> {
        val weather = (uiState.value as? WeatherUiState.Success)?.weather
        if (weather == null) {
            Log.d("DEBUG", "Weather data is null or not loaded yet")
            return emptyList()
        }

        val timeseries = weather.properties.timeseries
        if (timeseries.isEmpty()) return emptyList()

        return listOf(timeseries.first())
    }


    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return DashboardViewModel() as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}


data class WeatherData(
    val temperature: Double,
    val condition: String,
    val windSpeed: Double
) 