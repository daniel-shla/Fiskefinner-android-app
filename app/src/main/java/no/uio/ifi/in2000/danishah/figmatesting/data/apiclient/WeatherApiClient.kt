package no.uio.ifi.in2000.danishah.figmatesting.data.apiclient


import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.WeatherResponse

interface WeatherApiService {
    suspend fun getWeather(latitude: Double, longitude: Double): WeatherResponse
}

class WeatherApiServiceImpl : WeatherApiService {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    override suspend fun getWeather(latitude: Double, longitude: Double): WeatherResponse {
        val url = "https://api.met.no/weatherapi/locationforecast/2.0/compact?lat=$latitude&lon=$longitude"

        return try {
            val response = client.get(url) {
                headers {
                    append(HttpHeaders.UserAgent, "test weather/1.0 (nordbyesigurd@gmail.com)")
                }
            }

            if (response.status != HttpStatusCode.OK) {
                throw Exception("HTTP Error: ${response.status}")
            }

            response.body()
        } catch (e: Exception) {
            throw Exception("Error fetching weather data: ${e.message}")
        }
    }

}
