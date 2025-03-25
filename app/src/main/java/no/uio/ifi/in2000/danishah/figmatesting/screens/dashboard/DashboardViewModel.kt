package no.uio.ifi.in2000.danishah.figmatesting.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {
    // Weather data state
    private val _weatherData = MutableStateFlow<WeatherData?>(null)
    val weatherData: StateFlow<WeatherData?> = _weatherData.asStateFlow()
    
    init {
        // Initialize weather data
        loadWeatherData()
    }
    
    private fun loadWeatherData() {
        viewModelScope.launch {
            // TODO: Implement weather data loading
            // Nå har jeg bare DUMMY DATA
            _weatherData.value = WeatherData(
                temperature = 20.0,
                condition = "Sunny",
                windSpeed = 5.0
            )
        }
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