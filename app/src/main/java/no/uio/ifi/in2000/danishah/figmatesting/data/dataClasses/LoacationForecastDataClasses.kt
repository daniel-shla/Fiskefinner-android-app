import kotlinx.serialization.Serializable

@Serializable
data class WeatherResponse(val properties: Properties)

@Serializable
data class Properties(val timeseries: List<TimeSeries>)

@Serializable
data class TimeSeries(
    val time: String,
    val data: Data
)

@Serializable
data class Data(val instant: Instant)

@Serializable
data class Instant(val details: Details)

@Serializable
data class Details(
    val air_pressure_at_sea_level: Double,
    val air_temperature: Double,
    val cloud_area_fraction: Double,
    val wind_speed: Double,
)

sealed class WeatherUiState {
    object Loading : WeatherUiState()
    data class Success(val weather: WeatherResponse) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}

