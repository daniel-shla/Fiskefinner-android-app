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

class OwnMapViewModel(
    private val repository: LocationRepository = LocationRepository()
) : ViewModel() {

    // === UI State ===

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val searchResults = repository.searchResults
    val isLoading = repository.isLoading

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive = _isSearchActive.asStateFlow()

    private val _selectedSuggestion = MutableStateFlow<SearchSuggestion?>(null)
    val selectedSuggestion = _selectedSuggestion.asStateFlow()

    private val _mapCenter = MutableStateFlow(LocationDataSource.NORWAY_CENTER)
    val mapCenter = _mapCenter.asStateFlow()

    private val _zoomLevel = MutableStateFlow(LocationDataSource.COUNTRY_ZOOM)
    val zoomLevel = _zoomLevel.asStateFlow()

    private val _shouldDraw = MutableStateFlow(false)
    val shouldDraw: StateFlow<Boolean> = _shouldDraw

    private val _clusters = MutableStateFlow<List<Cluster>>(emptyList())
    val clusters: StateFlow<List<Cluster>> = _clusters

    private var searchJob: Job? = null

    // Viser hint hvis søk er aktivt og søketeksten er kortere enn 3 tegn
    val showMinCharsHint = searchQuery
        .combine(_isSearchActive) { query, active ->
            query.isNotEmpty() && query.length < 3 && active
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun triggerDraw() {
        _shouldDraw.value = true
    }

    // Oppdaterer søketekst og starter debounced søk om nødvendig
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        _selectedSuggestion.value = null

        if (query.length >= 3) {
            _isSearchActive.value = true
            searchJob = viewModelScope.launch {
                delay(300)
                repository.searchLocations(query, _mapCenter.value)
            }
        } else if (query.isEmpty()) {
            repository.resetSearchResults()
            _isSearchActive.value = false
        }
    }

    // Når bruker velger et forslag, brukes det som nytt søk
    fun selectSuggestion(suggestion: SearchSuggestion) {
        _selectedSuggestion.value = suggestion
        _searchQuery.value = suggestion.name
    }

    // Åpner eller lukker søk, og rydder opp i resultater
    fun setSearchActive(active: Boolean) {
        _isSearchActive.value = active
        if (!active) {
            repository.resetSearchResults()
            _selectedSuggestion.value = null
        }
    }

    // Søk og naviger til første resultat dersom brukeren ikke har valgt noe spesifikt
    fun searchAndNavigate(query: String) {
        selectedSuggestion.value?.let {
            navigateToLocation(it)
            setSearchActive(false)
            return
        }

        if (query.length < 3) return

        viewModelScope.launch {
            repository.searchLocations(query, _mapCenter.value)
            delay(500)
            val results = searchResults.value

            if (results.isNotEmpty()) {
                navigateToLocation(results.first())
                setSearchActive(false)
            } else {
                setSearchActive(true)
            }
        }
    }

    // Kalkulerer avstand mellom to punkter (Haversine-formelen)
    fun haversineDistance(a: Point, b: Point): Double {
        val R = 6371000.0 // Radius of Earth in meters
        val dLat = Math.toRadians(b.latitude() - a.latitude())
        val dLon = Math.toRadians(b.longitude() - a.longitude())
        val lat1 = Math.toRadians(a.latitude())
        val lat2 = Math.toRadians(b.latitude())

        val aCalc = sin(dLat / 2).pow(2) + sin(dLon / 2).pow(2) * cos(lat1) * cos(lat2)
        val c = 2 * atan2(sqrt(aCalc), sqrt(1 - aCalc))

        return R * c
    }

    // Oppdaterer klustere basert på zoom og lokasjoner
    fun updateClusters(locations: List<MittFiskeLocation>, zoom: Double) {
        Log.d("ClusterDebug", "Zoom: $zoom, antall locations: ${locations.size}")
        _clusters.value = clusterLocations(locations, zoom)
    }

    // Grupperer lokasjoner innenfor en radius basert på zoom
    fun clusterLocations(locations: List<MittFiskeLocation>, zoom: Double): List<Cluster> {
        val maxDistance = when (zoom.toInt()) {
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

        if (maxDistance == 0.0) {
            return locations.map { Cluster(it.toPoint(), listOf(it)) }
        }

        val clusters = mutableListOf<Cluster>()
        for (loc in locations) {
            val point = loc.toPoint()
            val existing = clusters.find {
                haversineDistance(it.center, point) < maxDistance
            }

            if (existing != null) {
                val updated = existing.spots + loc
                clusters.remove(existing)
                clusters.add(Cluster(existing.center, updated))
            } else {
                clusters.add(Cluster(point, listOf(loc)))
            }
        }

        Log.d("ClusterDebug", "Antall clusters generert: ${clusters.size}")
        return clusters
    }

    // Flytter kartet til valgt forslag
    fun navigateToLocation(suggestion: SearchSuggestion) {
        viewModelScope.launch {
            repository.getLocationDetails(suggestion.mapboxId)?.let { point ->
                _mapCenter.value = point
                _zoomLevel.value = LocationDataSource.DETAIL_ZOOM
            }
        }
    }

    // Flytter kartet direkte til et punkt
    fun navigateToPoint(point: Point) {
        _mapCenter.value = point
        _zoomLevel.value = LocationDataSource.DETAIL_ZOOM
    }

    // Oppdaterer kartets posisjon og zoom-nivå
    fun updateMapPosition(center: Point, zoom: Double) {
        _mapCenter.value = center
        _zoomLevel.value = zoom
    }

    // Øker zoomnivået (maks 18)
    fun zoomIn() {
        _zoomLevel.value = (_zoomLevel.value + 1.0).coerceAtMost(18.0)
    }

    // Senker zoomnivået (min 1)
    fun zoomOut() {
        _zoomLevel.value = (_zoomLevel.value - 1.0).coerceAtLeast(1.0)
    }

    // Factory for ViewModel (for bruk i f.eks. Hilt eller manuell initiering)
    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
                    return MapViewModel() as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
