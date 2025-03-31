package no.uio.ifi.in2000.danishah.figmatesting.data.repository

import WeatherResponse
import no.uio.ifi.in2000.danishah.figmatesting.screens.dashboard.LoactionForecast.WeatherApiServiceImpl

class WeatherRepository() {

    private val weatherApiService: WeatherApiServiceImpl = WeatherApiServiceImpl()

    suspend fun getWeather(lat: Double, lon: Double): WeatherResponse {
        return weatherApiService.getWeather(lat, lon)
    }
}
