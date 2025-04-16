package no.uio.ifi.in2000.danishah.figmatesting.screens.dashboard.locationForecast

import WeatherUiState
import TimeSeries
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.danishah.figmatesting.data.repository.WeatherRepository

class GPTWeatherViewModel : ViewModel() {

    private val weatherRepository = WeatherRepository()

    private val _weatherState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val weatherState: StateFlow<WeatherUiState> get() = _weatherState

    init {
        loadWeatherData(defaultLatitude, defaultLongitude)
    }

    private fun loadWeatherData(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _weatherState.value = WeatherUiState.Loading
            runCatching {
                weatherRepository.getWeather(latitude, longitude)
            }.onSuccess { data ->
                _weatherState.value = WeatherUiState.Success(data)
            }.onFailure { exception ->
                _weatherState.value = WeatherUiState.Error("Error fetching weather data: ${exception.message}")
            }
        }
    }

    fun fetchWeatherForNextTenDays(): List<TimeSeries> =
        extractTimeseries()?.groupBy { it.time.substring(0, 10) }
            ?.values?.mapNotNull { it.firstOrNull() }
            ?.take(10) ?: emptyList()

    fun fetchCurrentWeather(): List<TimeSeries> =
        extractTimeseries()?.let { listOf(it.first()) } ?: emptyList()

    fun predictWeatherDetails(): Triple<Double, Double, Double> =
        extractTimeseries()?.firstOrNull()?.data?.instant?.details?.let {
            Triple(it.air_temperature ?: 0.0, it.wind_speed ?: 0.0, precipitationDefault)
        } ?: Triple(0.0, 0.0, precipitationDefault)

    private fun extractTimeseries(): List<TimeSeries>? =
        (weatherState.value as? WeatherUiState.Success)?.weather?.properties?.timeseries?.takeUnless { it.isEmpty() }

    companion object {
        private const val defaultLatitude = 59.9139
        private const val defaultLongitude = 10.7522
        private const val precipitationDefault = 0.0
    }
}
