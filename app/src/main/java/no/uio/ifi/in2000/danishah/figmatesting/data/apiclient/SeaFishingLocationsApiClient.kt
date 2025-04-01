package no.uio.ifi.in2000.danishah.figmatesting.data.apiclient

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.FishingLocation
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.FishingLocationResponse
import no.uio.ifi.in2000.danishah.figmatesting.data.source.SeaFishingLocationsDataSource


class SeaFishingLocationsApiClient {
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                coerceInputValues = true
            })
        }
    }

    private val dataSource = SeaFishingLocationsDataSource()
    private val baseUrl = "https://gis.fiskeridir.no/server/rest/services/Yggdrasil/Kystnære_fiskeridata/MapServer/4/query"

    suspend fun getFishingLocations(offset: Int = 0, limit: Int = 2000): List<FishingLocation> {
        return try {
            val response = fetchFromApi(offset, limit)
            dataSource.processApiResponse(response)
        } catch (e: Exception) {
            Log.e("FishingLocationsApi", "Error fetching locations", e)
            emptyList()
        }
    }

    private suspend fun fetchFromApi(offset: Int = 0, limit: Int = 2000): FishingLocationResponse {
        try {
            val url = "$baseUrl?where=1%3D1" +
                    "&outFields=stedsnavn,alle_arter,SHAPE" +
                    "&resultOffset=$offset" +
                    "&resultRecordCount=$limit" +
                    "&outSR=4326" +
                    "&f=json"

            val response = client.get(url)
            val responseText = response.bodyAsText()
            
            Log.d("FishingLocationsApi", "API Response received")

            return if (responseText.contains("error")) {
                Log.e("FishingLocationsApi", "API Error: $responseText")
                FishingLocationResponse()
            } else {
                Json.decodeFromString<FishingLocationResponse>(responseText)
            }
        } catch (e: Exception) {
            Log.e("FishingLocationsApi", "API Exception", e)
            return FishingLocationResponse()
        }
    }

    fun filterByFishTypes(locations: List<FishingLocation>, fishTypes: List<String>): List<FishingLocation> {
        if (fishTypes.isEmpty()) return locations

        return locations.filter { location ->
            fishTypes.any { fishType ->
                location.fishTypes.contains(fishType)
            }
        }
    }
} 