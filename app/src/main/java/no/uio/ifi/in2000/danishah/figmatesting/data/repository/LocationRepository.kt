package no.uio.ifi.in2000.danishah.figmatesting.data.repository

import com.mapbox.geojson.Point
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import no.uio.ifi.in2000.danishah.figmatesting.data.model.SearchSuggestion
import no.uio.ifi.in2000.danishah.figmatesting.data.source.LocationDataSource

class LocationRepository(
    private val dataSource: LocationDataSource = LocationDataSource(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    
    // Current search results
    private val _searchResults = MutableStateFlow<List<SearchSuggestion>>(emptyList())
    val searchResults = _searchResults.asStateFlow()
    
    // State for loading indicator
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    

    suspend fun searchLocations(
        query: String,
        center: Point = LocationDataSource.NORWAY_CENTER
    ) = withContext(ioDispatcher) {
        if (query.length >= 2) {
            _isLoading.value = true
            try {
                val results = dataSource.getSearchSuggestions(query, center)
                _searchResults.value = results
            } finally {
                _isLoading.value = false
            }
        } else {
            _searchResults.value = emptyList()
        }
    }
    

    suspend fun getLocationDetails(mapboxId: String): Point? = withContext(ioDispatcher) {
        try {
            _isLoading.value = true
            return@withContext dataSource.retrieveLocation(mapboxId)
        } finally {
            _isLoading.value = false
        }
    }

    fun resetSearchResults() {
        _searchResults.value = emptyList()
        dataSource.resetSessionToken()
    }

//    fun close() {
//        dataSource.close()
//    }
} 