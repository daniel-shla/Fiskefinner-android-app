package no.uio.ifi.in2000.danishah.figmatesting.screens.dashboard.LoactionForecast

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.TimeSeries
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.WeatherUiState
import no.uio.ifi.in2000.danishah.figmatesting.data.repository.WeatherRepository

class WeatherViewModel : ViewModel() {

    private val repository: WeatherRepository = WeatherRepository()

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    private val uiState: StateFlow<WeatherUiState> = _uiState

    init {
        fetchWeather(59.9139, 10.7522) // Oslo coordinates
    }

    private fun fetchWeather(lat: Double, lon: Double) { // warning, but if the user accepts to share location the values change
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            try {
                val data = repository.getWeather(lat, lon)
                _uiState.value = WeatherUiState.Success(data)
            } catch (e: Exception) {
                _uiState.value = WeatherUiState.Error("Error collecting weather data: ${e.message}")
            }
        }
    }

    suspend fun getWeatherForLocation(lat: Double, lon: Double): TimeSeries {
        Log.d("WEATHER_API", "LOADING Weather data")

        return repository.getWeather(lat, lon)
            .properties
            .timeseries
            .first() // returns weather data for current time
    }


}
