package no.uio.ifi.in2000.danishah.figmatesting.screens.map

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
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.SearchSuggestion
import no.uio.ifi.in2000.danishah.figmatesting.data.repository.LocationRepository
import no.uio.ifi.in2000.danishah.figmatesting.data.source.LocationDataSource


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