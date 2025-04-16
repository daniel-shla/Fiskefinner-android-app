package no.uio.ifi.in2000.danishah.figmatesting.screens.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mapbox.geojson.Point
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.*
import no.uio.ifi.in2000.danishah.figmatesting.data.repository.LocationRepository
import no.uio.ifi.in2000.danishah.figmatesting.data.source.LocationDataSource
import kotlin.math.*

class GPTMapViewModel(
    private val locationRepository: LocationRepository = LocationRepository()
) : ViewModel() {

    private val currentSearchQuery = MutableStateFlow("")
    val searchSuggestions: StateFlow<List<SearchSuggestion>> = locationRepository.searchResults
    val isLoading: StateFlow<Boolean> = locationRepository.isLoading
    private val searchActive = MutableStateFlow(false)
    private val chosenSuggestion = MutableStateFlow<SearchSuggestion?>(null)
    private val mapCenterPosition = MutableStateFlow(LocationDataSource.NORWAY_CENTER)
    private val mapZoomLevel = MutableStateFlow(LocationDataSource.COUNTRY_ZOOM)
    private var ongoingSearchJob: Job? = null

    private val readyToDraw = MutableStateFlow(false)
    private val _locationClusters = MutableStateFlow<List<Cluster>>(emptyList())
    val locationClusters: StateFlow<List<Cluster>> = _locationClusters.asStateFlow()

    val displayMinCharsHint = currentSearchQuery.combine(searchActive) { query, active ->
        query.isNotEmpty() && query.length < 3 && active
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun markReadyToDraw() {
        readyToDraw.value = true
    }

    fun updateSearchQuery(query: String) {
        currentSearchQuery.value = query
        ongoingSearchJob?.cancel()
        chosenSuggestion.value = null

        if (query.length >= 3) {
            searchActive.value = true
            ongoingSearchJob = viewModelScope.launch {
                delay(300)
                locationRepository.searchLocations(query, mapCenterPosition.value)
            }
        } else if (query.isEmpty()) {
            resetSearchResults()
            searchActive.value = false
        }
    }

    private fun resetSearchResults() {
        locationRepository.resetSearchResults()
        chosenSuggestion.value = null
    }

    fun selectSuggestion(suggestion: SearchSuggestion) {
        chosenSuggestion.value = suggestion
        currentSearchQuery.value = suggestion.name
    }

    fun updateSearchActiveState(active: Boolean) {
        searchActive.value = active
        if (!active) {
            resetSearchResults()
        }
    }

    fun searchAndNavigate(query: String) {
        if (query.length < 3) return

        viewModelScope.launch {
            locationRepository.searchLocations(query, mapCenterPosition.value)
            delay(500)
            searchSuggestions.value.firstOrNull()?.let {
                navigateToSuggestionLocation(it)
                updateSearchActiveState(false)
            } ?: updateSearchActiveState(true)
        }
    }

    private val earthRadiusMeters = 6371_000.0

    fun haversineDistance(from: Point, to: Point): Double {
        val (lat1, lon1, lat2, lon2) = listOf(from.latitude(), from.longitude(), to.latitude(), to.longitude()).map(Math::toRadians)
        val dLat = lat2 - lat1
        val dLon = lon2 - lon1

        val a = sin(dLat / 2).pow(2.0) + sin(dLon / 2).pow(2.0) * cos(lat1) * cos(lat2)
        return 2 * earthRadiusMeters * atan2(sqrt(a), sqrt(1 - a))
    }

    fun updateClusters(locations: List<MittFiskeLocation>, zoom: Double) {
        Log.d("ClusterDebug", "Zoom: $zoom, num of locations: ${locations.size}")
        _locationClusters.value = calculateClusterLocations(locations, zoom)
    }

    fun calculateClusterLocations(locations: List<MittFiskeLocation>, zoom: Double): List<Cluster> {
        val maxDistance = getMaxDistanceBasedOnZoom(zoom.toInt())

        if (maxDistance == 0.0) return locations.map { Cluster(it.toPoint(), listOf(it)) }

        val clusters = mutableListOf<Cluster>()

        locations.forEach { loc ->
            val locPoint = loc.toPoint()
            clusters.find { haversineDistance(it.center, locPoint) < maxDistance }?.let { existingCluster ->
                clusters.remove(existingCluster)
                clusters.add(Cluster(existingCluster.center, existingCluster.spots + loc))
            } ?: clusters.add(Cluster(locPoint, listOf(loc)))
        }

        Log.d("ClusterDebug", "Generated cluster count: ${clusters.size}")
        return clusters
    }

    private fun getMaxDistanceBasedOnZoom(zoomLevel: Int): Double = when (zoomLevel) {
        in 0..4 -> 250_000.0
        5 -> 220_000.0
        6 -> 210_000.0
        7 -> 30_000.0
        8 -> 20_000.0
        9 -> 13_000.0
        10 -> 8_000.0
        11 -> 6_000.0
        12 -> 4_000.0
        else -> 0.0
    }

    fun navigateToSuggestionLocation(suggestion: SearchSuggestion) {
        viewModelScope.launch {
            locationRepository.getLocationDetails(suggestion.mapboxId)?.let {
                mapCenterPosition.value = it
                mapZoomLevel.value = LocationDataSource.DETAIL_ZOOM
            }
        }
    }

    fun navigateToPoint(point: Point) {
        mapCenterPosition.value = point
        mapZoomLevel.value = LocationDataSource.DETAIL_ZOOM
    }

    fun updateMapPosition(center: Point, zoom: Double) {
        mapCenterPosition.value = center
        mapZoomLevel.value = zoom
    }

    fun zoomIn() {
        mapZoomLevel.value = (mapZoomLevel.value + 1.0).coerceAtMost(18.0)
    }

    fun zoomOut() {
        mapZoomLevel.value = (mapZoomLevel.value - 1.0).coerceAtLeast(1.0)
    }

    companion object {
        val factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
                    return GPTMapViewModel(locationRepository = LocationRepository()) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
