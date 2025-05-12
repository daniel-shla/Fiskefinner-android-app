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

    fun getWeatherForPrediction(): Triple<Double, Double, Double> {
        val weather = (uiState.value as? WeatherUiState.Success)?.weather ?: return Triple(0.0, 0.0, 0.0)

        val timeseries = weather.properties.timeseries.firstOrNull()
        val temperature = timeseries?.data?.instant?.details?.air_temperature ?: 0.0
        val windSpeed = timeseries?.data?.instant?.details?.wind_speed ?: 0.0
        val precipitation = 0.0 //timeseries?.data?.instant?.details?.precipitation_amount ?: 0.0

        return Triple(temperature, windSpeed, precipitation)
    }

    suspend fun getWeatherForLocation(lat: Double, lon: Double): TimeSeries {
        Log.d("WEATHER_API", "LOADING Weather data")

        return repository.getWeather(lat, lon)
            .properties
            .timeseries
            .first() // returnerer værdata for nåværende tidspunkt
    }


}
