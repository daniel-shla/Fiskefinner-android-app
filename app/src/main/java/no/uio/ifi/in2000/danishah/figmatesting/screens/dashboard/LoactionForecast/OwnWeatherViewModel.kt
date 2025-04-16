package no.uio.ifi.in2000.danishah.figmatesting.screens.dashboard.locationForecast

import TimeSeries
import WeatherUiState
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.danishah.figmatesting.data.repository.WeatherRepository

class OwnWeatherViewModel : ViewModel() {

    // Repository som henter værdata
    private val repository = WeatherRepository()

    // Holder tilstanden til værvisningen (laster, suksess, feil)
    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState

    // Initielt værkall med Oslos koordinater
    init {
        fetchWeather(59.9139, 10.7522) // Oslo
    }

    // Henter værdata og oppdaterer tilstanden
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

    // Returnerer én datapunkt per dag for de neste 10 dagene
    fun getNext10DaysWeather(): List<TimeSeries> {
        val weather = (uiState.value as? WeatherUiState.Success)?.weather ?: run {
            Log.d("WeatherViewModel", "Weather data is null or not loaded yet")
            return emptyList()
        }

        val timeseries = weather.properties.timeseries
        Log.d("WeatherViewModel", "Total timeseries count: ${timeseries.size}")

        val grouped = timeseries.groupBy { it.time.substring(0, 10) } // Gruppér etter dato (YYYY-MM-DD)
        Log.d("WeatherViewModel", "Grouped days count: ${grouped.keys.size}")

        val next10Days = grouped.values.mapNotNull { it.firstOrNull() }.take(10)
        Log.d("WeatherViewModel", "Next 10 days count: ${next10Days.size}")

        return next10Days
    }

    // Returnerer værdata for nåværende tidspunkt
    fun getCurrentWeather(): List<TimeSeries> {
        val weather = (uiState.value as? WeatherUiState.Success)?.weather ?: run {
            Log.d("WeatherViewModel", "Weather data is null or not loaded yet")
            return emptyList()
        }

        val timeseries = weather.properties.timeseries
        return if (timeseries.isNotEmpty()) listOf(timeseries.first()) else emptyList()
    }

    // Returnerer verdier som kan brukes til prediksjon (temp, vind, nedbør)
    fun getWeatherForPrediction(): Triple<Double, Double, Double> {
        val timeseries = (uiState.value as? WeatherUiState.Success)
            ?.weather
            ?.properties
            ?.timeseries
            ?.firstOrNull()

        val temperature = timeseries?.data?.instant?.details?.air_temperature ?: 0.0
        val windSpeed = timeseries?.data?.instant?.details?.wind_speed ?: 0.0
        val precipitation = 0.0 // Fremtidig støtte: legg til faktisk verdi hvis tilgjengelig

        return Triple(temperature, windSpeed, precipitation)
    }
}
