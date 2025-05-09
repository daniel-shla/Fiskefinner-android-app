package no.uio.ifi.in2000.danishah.figmatesting.screens.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mapbox.geojson.Point
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.Cluster
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.MittFiskeLocation
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.SearchSuggestion
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.toPoint
import no.uio.ifi.in2000.danishah.figmatesting.data.repository.LocationRepository
import no.uio.ifi.in2000.danishah.figmatesting.data.source.LocationDataSource
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt


class MapViewModel(private val repository: LocationRepository = LocationRepository()) : ViewModel() {
    
    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    
    // Search results from repository
    val searchResults = repository.searchResults
    
    // Loading state
    val isLoading = repository.isLoading
    
    // Search active state
    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive = _isSearchActive.asStateFlow()
    
    // Selected suggestion
    private val _selectedSuggestion = MutableStateFlow<SearchSuggestion?>(null)
    val selectedSuggestion = _selectedSuggestion.asStateFlow()
    
    // Current map center
    private val _mapCenter = MutableStateFlow(LocationDataSource.NORWAY_CENTER)
    val mapCenter = _mapCenter.asStateFlow()
    
    // Current zoom level
    private val _zoomLevel = MutableStateFlow(LocationDataSource.COUNTRY_ZOOM)
    val zoomLevel = _zoomLevel.asStateFlow()
    
    // Debounce search job
    private var searchJob: Job? = null

    private val _shouldDraw = MutableStateFlow(false)
    val shouldDraw: StateFlow<Boolean> = _shouldDraw

    private val _clusters = MutableStateFlow<List<Cluster>>(emptyList())
    val clusters: StateFlow<List<Cluster>> = _clusters

    fun triggerDraw() {
        _shouldDraw.value = true
    }


    // State that combines search query and min chars
    val showMinCharsHint = searchQuery.combine(_isSearchActive) { query, active ->
        query.isNotEmpty() && query.length < 3 && active
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    //Update search query and trigger search
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        
        // Cancel previous search job
        searchJob?.cancel()
        
        // Clear selected suggestion when query changes
        _selectedSuggestion.value = null
        
        if (query.length >= 3) {
            // Show search results popup as user types
            _isSearchActive.value = true
            
            // Debounce search
            searchJob = viewModelScope.launch {
                delay(300) // 300ms debounce
                repository.searchLocations(query, _mapCenter.value)
            }
        } else {
            if (query.isEmpty()) {
                repository.resetSearchResults()
                _isSearchActive.value = false
            }
        }
    }
    
    fun selectSuggestion(suggestion: SearchSuggestion) {
        _selectedSuggestion.value = suggestion
        _searchQuery.value = suggestion.name
    }

    fun setSearchActive(active: Boolean) {
        _isSearchActive.value = active
        if (!active) {
            // Clear results and selected suggestion when search is closed
            repository.resetSearchResults()
            _selectedSuggestion.value = null
        }
    }
    

    fun searchAndNavigate(query: String) {
        // If we already have a selected suggestion, navigate to it
        selectedSuggestion.value?.let {
            navigateToLocation(it)
            setSearchActive(false)
            return
        }
        
        // Otherwise perform a search and navigate to first result
        if (query.length < 3) return
        
        viewModelScope.launch {
            // First search for the location
            repository.searchLocations(query, _mapCenter.value)

            delay(500)
            
            // Navigate to the first result if available
            val results = searchResults.value
            if (results.isNotEmpty()) {
                navigateToLocation(results.first())
                setSearchActive(false)
            } else {
                // If no results, keep the search dialog open
                setSearchActive(true)
            }
        }
    }

    fun haversineDistance(a: Point, b: Point): Double {
        val R = 6371000.0
        val dLat = Math.toRadians(b.latitude() - a.latitude())
        val dLon = Math.toRadians(b.longitude() - a.longitude())
        val lat1 = Math.toRadians(a.latitude())
        val lat2 = Math.toRadians(b.latitude())

        val aCalc = sin(dLat / 2).pow(2.0) + sin(dLon / 2).pow(2.0) * cos(lat1) * cos(lat2)
        val c = 2 * atan2(sqrt(aCalc), sqrt(1 - aCalc))

        return R * c
    }



    fun updateClusters(locations: List<MittFiskeLocation>, zoom: Double) {
        Log.d("ClusterDebug", "Zoom: $zoom, antall locations: ${locations.size}")

        _clusters.value = clusterLocations(locations, zoom)
    }

    fun clusterLocations(locations: List<MittFiskeLocation>, zoom: Double): List<Cluster> {
        val ratedLocations = locations.filter { it.rating != null }

        val maxDistance = when (zoom.toInt()) {
            in 0..4 -> 250000.0
            5 -> 220000.0
            6 -> 210000.0
            7 -> 30000.0
            8 -> 20000.0
            9 -> 13000.0
            10 -> 8000.0
            11 -> 6000.0
            12 -> 4000.0
            else -> 0.0
        }

        if (maxDistance == 0.0) {
            return ratedLocations.map { loc ->
                Cluster(
                    center = loc.toPoint(),
                    spots = listOf(loc),
                    averageRating = loc.rating?.toFloat()
                )
            }
        }

        val clusters = mutableListOf<Cluster>()

        for (loc in ratedLocations) {
            val point = loc.toPoint()

            val existing = clusters.find {
                haversineDistance(it.center, point) < maxDistance
            }

            if (existing != null) {
                val updatedSpots = existing.spots + loc
                val ratings = updatedSpots.mapNotNull { it.rating }
                val avgRating = if (ratings.isNotEmpty()) ratings.average().toFloat() else null

                clusters.remove(existing)
                clusters.add(
                    Cluster(
                        center = existing.center,
                        spots = updatedSpots,
                        averageRating = avgRating
                    )
                )
            } else {
                clusters.add(
                    Cluster(
                        center = point,
                        spots = listOf(loc),
                        averageRating = loc.rating?.toFloat()
                    )
                )
            }
        }

        Log.d("ClusterDebug", "Zoom: $zoom, total: ${locations.size}, rated: ${ratedLocations.size}, clusters: ${clusters.size}")
        return clusters
    }





    // Navigate to a location
    fun navigateToLocation(suggestion: SearchSuggestion) {
        viewModelScope.launch {
            val point = repository.getLocationDetails(suggestion.mapboxId)
            if (point != null) {
                _mapCenter.value = point
                _zoomLevel.value = LocationDataSource.DETAIL_ZOOM
            }
        }
    }
    
    // Navigate directly to a point
    fun navigateToPoint(point: Point) {
        _mapCenter.value = point
        _zoomLevel.value = LocationDataSource.DETAIL_ZOOM
    }
    
    // Update map position
    fun updateMapPosition(center: Point, zoom: Double) {
        _mapCenter.value = center
        _zoomLevel.value = zoom
    }
    
    // Zoom in
    fun zoomIn() {
        _zoomLevel.value = (_zoomLevel.value + 1.0).coerceAtMost(18.0)
    }
    
    // Zoom out
    fun zoomOut() {
        _zoomLevel.value = (_zoomLevel.value - 1.0).coerceAtLeast(1.0)
    }
    
    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
                    return MapViewModel(
                        repository = LocationRepository()
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
} 