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
    

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    
    val searchResults = repository.searchResults
    
    val isLoading = repository.isLoading
    
    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive = _isSearchActive.asStateFlow()
    
    private val _selectedSuggestion = MutableStateFlow<SearchSuggestion?>(null)
    private val selectedSuggestion = _selectedSuggestion.asStateFlow()
    
    private val _mapCenter = MutableStateFlow(LocationDataSource.NORWAY_CENTER)
    val mapCenter = _mapCenter.asStateFlow()
    
    private val _zoomLevel = MutableStateFlow(LocationDataSource.COUNTRY_ZOOM)
    val zoomLevel = _zoomLevel.asStateFlow()
    
    private var searchJob: Job? = null

    private val _shouldDraw = MutableStateFlow(false)

    private val _clusters = MutableStateFlow<List<Cluster>>(emptyList())
    val clusters: StateFlow<List<Cluster>> = _clusters

    fun triggerDraw() {
        _shouldDraw.value = true
    }


    // State that combines search query and min chars
    val showMinCharsHint = searchQuery.combine(_isSearchActive) { query, active ->
        query.isNotEmpty() && query.length < 3 && active
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        
        searchJob?.cancel()
        
        _selectedSuggestion.value = null
        
        if (query.length >= 3) {
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
        
        // Otherwise: search and navigate to first result
        if (query.length < 3) return
        
        viewModelScope.launch {
            repository.searchLocations(query, _mapCenter.value)

            delay(500)
            
            // Nav to first result if available
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

    private fun haversineDistance(a: Point, b: Point): Double {
        val r = 6371000.0
        val dLat = Math.toRadians(b.latitude() - a.latitude())
        val dLon = Math.toRadians(b.longitude() - a.longitude())
        val lat1 = Math.toRadians(a.latitude())
        val lat2 = Math.toRadians(b.latitude())

        val aCalc = sin(dLat / 2).pow(2.0) + sin(dLon / 2).pow(2.0) * cos(lat1) * cos(lat2)
        val c = 2 * atan2(sqrt(aCalc), sqrt(1 - aCalc))

        return r * c
    }



    fun updateClusters(locations: List<MittFiskeLocation>, zoom: Double) {
        Log.d("ClusterDebug", "Zoom: $zoom, antall locations: ${locations.size}")

        _clusters.value = clusterLocations(locations, zoom)
    }
/*

    //Older more rigid function for clustering MittFiskeLocations

    private fun clusterLocations(locations: List<MittFiskeLocation>, zoom: Double): List<Cluster> {
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
                //avgRating = if (ratings.contains(4)) 4.toFloat() else avgRating

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
*/

    //Help function for smoother and dynamic calculation of clustering

    private fun calculateMaxDistance(zoom: Double): Double {
        return 4000000.0 / (2.0.pow(zoom))  //Make clustering more aggressive by increasing first var here, make clustering less aggresive by dereasing it.
    }
    private fun clusterLocations(locations: List<MittFiskeLocation>, zoom: Double): List<Cluster> {
        val ratedLocations = locations.filter { it.rating != null }
        val maxDistance = calculateMaxDistance(zoom)

        if (maxDistance <= 0.0) {
            return ratedLocations.map { loc ->
                Cluster(loc.toPoint(), listOf(loc), loc.rating?.toFloat())
            }
        }

        val clusters = mutableListOf<Cluster>()

        for (loc in ratedLocations) {
            val point = loc.toPoint()

            val cluster = clusters.minByOrNull { haversineDistance(it.center, point) }

            if (cluster != null && haversineDistance(cluster.center, point) < maxDistance) {
                val newSpots = cluster.spots + loc
                val newRating = newSpots.mapNotNull { it.rating }.average().toFloat()
                clusters.remove(cluster)
                clusters.add(cluster.copy(spots = newSpots, averageRating = newRating))
            } else {
                clusters.add(Cluster(center = point, spots = listOf(loc), averageRating = loc.rating?.toFloat()))
            }
        }

        return clusters
    }




    fun navigateToLocation(suggestion: SearchSuggestion) {
        viewModelScope.launch {
            val point = repository.getLocationDetails(suggestion.mapboxId)
            if (point != null) {
                _mapCenter.value = point
                _zoomLevel.value = LocationDataSource.DETAIL_ZOOM
            }
        }
    }
    

    fun navigateToPoint(point: Point) {
        _mapCenter.value = point
        _zoomLevel.value = LocationDataSource.DETAIL_ZOOM
    }
    

    fun updateMapPosition(center: Point, zoom: Double) {
        _mapCenter.value = center
        _zoomLevel.value = zoom
    }

    fun zoomIn() {
        _zoomLevel.value = (_zoomLevel.value + 1.0).coerceAtMost(18.0)
    }
    

    fun zoomOut() {
        _zoomLevel.value = (_zoomLevel.value - 1.0).coerceAtLeast(1.0)
    }
    
    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
                    val viewModel = MapViewModel(repository = LocationRepository())
                    return modelClass.cast(viewModel)!!
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }

        }
    }
} 