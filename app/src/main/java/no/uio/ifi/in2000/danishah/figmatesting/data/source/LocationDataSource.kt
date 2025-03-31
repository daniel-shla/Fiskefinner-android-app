package no.uio.ifi.in2000.danishah.figmatesting.data.source

import com.mapbox.geojson.Point
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.SearchSuggestion
import no.uio.ifi.in2000.danishah.figmatesting.data.apiclient.MapboxApiClient
import java.util.UUID


class LocationDataSource(private val api: MapboxApiClient = MapboxApiClient()) {
    
    // Session token for Mapbox Search API (Must be randomly generated each time, i think??)
    private var sessionToken = UUID.randomUUID().toString()
    
    // Reset session token - called when a search session is completed
    fun resetSessionToken() {
        sessionToken = UUID.randomUUID().toString()
    }

    suspend fun getSearchSuggestions(
        query: String,
        center: Point = Point.fromLngLat(10.7522, 61.5), // Norway center
        limit: Int = 5
    ): List<SearchSuggestion> {
        if (query.length < 2) return emptyList()
        return api.getSuggestions(query, sessionToken, center, limit)
    }
    

    suspend fun retrieveLocation(mapboxId: String): Point? {
        return api.retrieveLocation(mapboxId, sessionToken)
    }
    
    companion object {
        val NORWAY_CENTER = Point.fromLngLat(10.7522, 61.5)
        val OSLO_LOCATION = Point.fromLngLat(10.6458, 59.8946)
        
        // Default zooms
        const val COUNTRY_ZOOM = 4.0
        const val DETAIL_ZOOM = 12.0
    }
} 