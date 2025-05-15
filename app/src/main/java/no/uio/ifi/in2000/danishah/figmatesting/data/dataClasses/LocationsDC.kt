package no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses

import kotlinx.serialization.Serializable

// IT LOOKS LIKE THERE ARE ERRORS/WARNINGS HERE BUT DO NOT TOUCH, APP REQUIRES THIS STATE

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
data class Data(
    val instant: Instant,
    val next_1_hours: NextHourData? = null,
    val next_6_hours: Next6HourData? = null
)

@Serializable
data class Instant(val details: Details)

// needed for precipitation
@Serializable
data class NextHourData(val details: NextHourDetails)

@Serializable
data class Next6HourData(val details: Next6HourDetails)

@Serializable
data class Details(
    val air_pressure_at_sea_level: Double,
    val air_temperature: Double,
    val cloud_area_fraction: Double,
    val wind_speed: Double,
)

@Serializable
data class NextHourDetails(
    val precipitation_amount: Double
)

@Serializable
data class Next6HourDetails(
    val precipitation_amount: Double
)

sealed class WeatherUiState {
    object Loading : WeatherUiState()
    data class Success(val weather: WeatherResponse) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}

