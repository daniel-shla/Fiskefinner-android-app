package no.uio.ifi.in2000.danishah.figmatesting.data.apiclient

import android.util.Log
import com.mapbox.geojson.Point
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.SearchSuggestion
import org.json.JSONObject
import java.net.URLEncoder


class MapboxApiClient {
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }
    
    private val apiKey = "pk.eyJ1IjoiZGFuaXNoYWhsYSIsImEiOiJjbThuZWJndGoxbWJnMmpxcml6N2wzZ2h4In0.8KD81RbnLUAziIMbp1aQoA"

    suspend fun getSuggestions(
        query: String,
        sessionToken: String,
        center: Point = Point.fromLngLat(10.7522, 61.5), // Norway center
        limit: Int = 5
    ): List<SearchSuggestion> {
        try {
            // Encode query for URL
            val encodedQuery = withContext(Dispatchers.IO) {
                URLEncoder.encode(query, "UTF-8")
            }
            
            // Build the URL for searching (DO NOT CHANGE)
            val url = "https://api.mapbox.com/search/searchbox/v1/suggest?" +
                    "q=$encodedQuery" +
                    "&country=NO" +
                    "&limit=$limit" +
                    "&proximity=${center.longitude()},${center.latitude()}" +
                    "&session_token=$sessionToken" +
                    "&access_token=$apiKey"



            val response = client.get(url)
            val responseText = response.bodyAsText()
            
            val jsonResponse = JSONObject(responseText)
            val suggestions = jsonResponse.getJSONArray("suggestions")
            val suggestionsList = mutableListOf<SearchSuggestion>()
            
            for (i in 0 until suggestions.length()) {
                val suggestion = suggestions.getJSONObject(i)
                suggestionsList.add(
                    SearchSuggestion(
                        name = suggestion.getString("name"),
                        mapboxId = suggestion.getString("mapbox_id"),
                        featureType = suggestion.getString("feature_type"),
                        address = if (suggestion.has("address")) suggestion.getString("address") else null,
                        fullAddress = if (suggestion.has("full_address")) suggestion.getString("full_address") else null,
                        placeFormatted = if (suggestion.has("place_formatted")) suggestion.getString("place_formatted") else null,
                        maki = if (suggestion.has("maki")) suggestion.getString("maki") else null
                    )
                )
            }
            
            return suggestionsList
        } catch (e: Exception) {
            Log.e("MapboxApiClient", "Error getting suggestions", e)
            return emptyList()
        }
    }

    suspend fun retrieveLocation(mapboxId: String, sessionToken: String): Point? {
        try {
            val url = "https://api.mapbox.com/search/searchbox/v1/retrieve/$mapboxId?" +
                    "session_token=$sessionToken" +
                    "&access_token=$apiKey"
            
            val response = client.get(url)
            val responseText = response.bodyAsText()
            
            val jsonResponse = JSONObject(responseText)
            val features = jsonResponse.getJSONArray("features")
            
            if (features.length() > 0) {
                val feature = features.getJSONObject(0)
                val geometry = feature.getJSONObject("geometry")
                val coordinates = geometry.getJSONArray("coordinates")
                val longitude = coordinates.getDouble(0)
                val latitude = coordinates.getDouble(1)
                
                return Point.fromLngLat(longitude, latitude)
            }
            
            return null
        } catch (e: Exception) {
            Log.e("MapboxApiClient", "Error retrieving location", e)
            return null
        }
    }
    
    fun close() {
        client.close()
    }
} 