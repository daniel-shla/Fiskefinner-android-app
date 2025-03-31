package no.uio.ifi.in2000.danishah.figmatesting.screens.dashboard.LoactionForecast

import TimeSeries
import WeatherUiState
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.danishah.figmatesting.data.repository.WeatherRepository

class WeatherViewModel() : ViewModel() {

    private val repository: WeatherRepository = WeatherRepository()

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState

    init {
        fetchWeather(59.9139, 10.7522) // Oslo coordinates
    }

    private fun fetchWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            try {
                val data = repository.getWeather(lat, lon)
                _uiState.value = WeatherUiState.Success(data)
            } catch (e: Exception) {
                _uiState.value = WeatherUiState.Error("Feil ved henting av værdata: ${e.message}")
            }
        }
    }

    fun getNext10DaysWeather(): List<TimeSeries> {
        val weather = (uiState.value as? WeatherUiState.Success)?.weather
        if (weather == null) {
            Log.d("DEBUG", "Weather data is null or not loaded yet")
            return emptyList()
        }

        val timeseries = weather.properties.timeseries
        Log.d("DEBUG", "Total timeseries count: ${timeseries.size}")

        val grouped = timeseries.groupBy { it.time.substring(0, 10) }
        Log.d("DEBUG", "Grouped days count: ${grouped.keys.size}")

        val next10Days = grouped.values.mapNotNull { it.firstOrNull() }.take(10)
        Log.d("DEBUG", "Next 10 days count: ${next10Days.size}")

        return next10Days
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

}
