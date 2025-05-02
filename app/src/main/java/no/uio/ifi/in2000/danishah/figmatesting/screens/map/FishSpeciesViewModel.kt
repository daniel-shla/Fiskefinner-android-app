package no.uio.ifi.in2000.danishah.figmatesting.screens.map

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.FishSpeciesData
import no.uio.ifi.in2000.danishah.figmatesting.data.repository.FishSpeciesRepository

/**
 * ViewModel for handling fish species selection and polygon display
 */
class FishSpeciesViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = FishSpeciesRepository(application.applicationContext)
    
    // List of all available fish species
    private val _availableSpecies = MutableStateFlow<List<FishSpeciesData>>(emptyList())
    val availableSpecies: StateFlow<List<FishSpeciesData>> = _availableSpecies.asStateFlow()
    
    // Map of species scientific name to its loaded data and enabled state
    private val _speciesStates = MutableStateFlow<Map<String, SpeciesState>>(emptyMap())
    val speciesStates: StateFlow<Map<String, SpeciesState>> = _speciesStates.asStateFlow()
    
    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Show species panel state
    private val _showSpeciesPanel = MutableStateFlow(false)
    val showSpeciesPanel: StateFlow<Boolean> = _showSpeciesPanel.asStateFlow()
    
    // Error message
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // Maximum number of species that can be enabled at once
    private val maxConcurrentSpecies = 8
    
    init {
        loadAvailableSpecies()
    }
    
    /**
     * Load all available fish species
     */
    private fun loadAvailableSpecies() {
        val species = repository.getAvailableFishSpecies()
        _availableSpecies.value = species
        
        // Initialize species states map
        val initialStates = species.associate { 
            it.scientificName to SpeciesState(
                species = it,
                isEnabled = false,
                isLoaded = false
            )
        }
        _speciesStates.value = initialStates
    }
    
    /**
     * Toggle a species on/off and load its data if needed
     */
    fun toggleSpecies(scientificName: String) {
        val currentStates = _speciesStates.value.toMutableMap()
        val currentState = currentStates[scientificName] ?: return
        
        // Toggle enabled state
        val newEnabled = !currentState.isEnabled
        
        // Check if enabling would exceed maximum concurrent species
        if (newEnabled) {
            val currentlyEnabled = currentStates.values.count { it.isEnabled }
            if (currentlyEnabled >= maxConcurrentSpecies) {
                _errorMessage.value = "Maksimalt $maxConcurrentSpecies arter kan vises samtidig."
                return
            }
        }
        
        viewModelScope.launch {
            // If enabling and not loaded yet, load the data
            if (newEnabled && !currentState.isLoaded) {
                _isLoading.value = true
                _errorMessage.value = null
                
                try {
                    val fishData = repository.loadFishSpeciesPolygons(scientificName)
                    
                    if (fishData != null) {
                        // Update with loaded data
                        currentStates[scientificName] = SpeciesState(
                            species = fishData,
                            isEnabled = true,
                            isLoaded = true
                        )
                    } else {
                        // Failed to load, but still toggle the state
                        currentStates[scientificName] = currentState.copy(
                            isEnabled = true
                        )
                        _errorMessage.value = "Kunne ikke laste data for ${FishSpeciesData.getCommonName(scientificName)}"
                    }
                } catch (e: Exception) {
                    // Just toggle the state if loading fails
                    currentStates[scientificName] = currentState.copy(
                        isEnabled = true
                    )
                    _errorMessage.value = "Feil ved lasting av data: ${e.message}"
                } finally {
                    _isLoading.value = false
                }
            } else {
                // Just toggle the enabled state
                currentStates[scientificName] = currentState.copy(
                    isEnabled = newEnabled
                )
            }
            
            // Update the states map
            _speciesStates.value = currentStates
        }
    }
    
    /**
     * Get all currently enabled species
     */
    fun getEnabledSpecies(): List<FishSpeciesData> {
        return _speciesStates.value.values
            .filter { it.isEnabled && it.isLoaded }
            .map { it.species }
    }
    
    /**
     * Toggle the species panel visibility
     */
    fun toggleSpeciesPanel(show: Boolean) {
        _showSpeciesPanel.value = show
    }
    
    /**
     * Clear error message
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
    
    /**
     * Data class to track species state
     */
    data class SpeciesState(
        val species: FishSpeciesData,
        val isEnabled: Boolean,
        val isLoaded: Boolean,
        val opacity: Float = 1.0f  // Default full opacity
    )
    
    /**
     * Update the opacity (visibility) of a species
     */
    fun updateSpeciesOpacity(scientificName: String, opacity: Float) {
        val currentStates = _speciesStates.value.toMutableMap()
        val currentState = currentStates[scientificName] ?: return
        
        currentStates[scientificName] = currentState.copy(opacity = opacity)
        _speciesStates.value = currentStates
    }
    
    /**
     * Factory for creating the ViewModel with the application context
     */
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FishSpeciesViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return FishSpeciesViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 