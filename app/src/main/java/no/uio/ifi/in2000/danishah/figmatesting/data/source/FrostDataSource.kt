package no.uio.ifi.in2000.danishah.figmatesting.data.source

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.FrostResponse

class FrostDataSource {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(Logging) {
            level = LogLevel.BODY
        }
    }

    suspend fun fetchFrostData(): FrostResponse {
        val apiKey = "fa254c11-8d5c-4dd0-9f37-7cdd1eba9816"
        return client.get("https://frost.met.no/observations/v0.jsonld") {
            headers {
                append("Authorization", apiKey)
            }
            // parameter("sources", "SN18700") // trengs ikke egt
            parameter("elements", "air_temperature, wind_speed, precipitation_amount")
        }.body()
    }
}
