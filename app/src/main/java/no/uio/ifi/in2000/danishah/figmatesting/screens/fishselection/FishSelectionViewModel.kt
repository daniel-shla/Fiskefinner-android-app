package no.uio.ifi.in2000.danishah.figmatesting.screens.fishselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FishSelectionViewModel : ViewModel() {
    // List of fish types with selection state
    private val _fishTypes = MutableStateFlow<List<FishType>>(emptyList())
    val fishTypes: StateFlow<List<FishType>> = _fishTypes.asStateFlow()
    
    // Selected fish count
    private val _selectedCount = MutableStateFlow(0)
    val selectedCount: StateFlow<Int> = _selectedCount.asStateFlow()
    
    init {
        loadFishTypes()
    }
    
    private fun loadFishTypes() {
        viewModelScope.launch {
            // Populate with dummy data - the actual names, descriptions, and habitats 
            // are loaded from string resources in the FishSelectionScreen
            _fishTypes.value = listOf(
                FishType(
                    id = 1,
                    name = "Atlantic Salmon",
                    description = "Commonly found in Norwegian rivers, known for their jumping ability",
                    habitat = "Rivers and coastal areas",
                    isSelected = false
                ),
                FishType(
                    id = 2,
                    name = "Brown Trout",
                    description = "Popular freshwater game fish with distinctive spotted pattern",
                    habitat = "Rivers, lakes, and streams",
                    isSelected = false
                ),
                FishType(
                    id = 3,
                    name = "Arctic Char",
                    description = "Cold water species found in mountain lakes and rivers",
                    habitat = "Deep, cold mountain lakes",
                    isSelected = false
                ),
                FishType(
                    id = 4,
                    name = "Cod",
                    description = "Most important commercial fish in Norway",
                    habitat = "Saltwater, coastal areas",
                    isSelected = false
                ),
                FishType(
                    id = 5,
                    name = "Mackerel",
                    description = "Fast-swimming pelagic fish, excellent for beginners",
                    habitat = "Coastal and open sea areas",
                    isSelected = false
                ),
                FishType(
                    id = 6,
                    name = "Pollock",
                    description = "Common coastal fish, good for beginning anglers",
                    habitat = "Coastal waters",
                    isSelected = false
                ),
                FishType(
                    id = 7,
                    name = "Pike",
                    description = "Aggressive predator with razor-sharp teeth",
                    habitat = "Lakes and slow-moving rivers",
                    isSelected = false
                ),
                FishType(
                    id = 8,
                    name = "Perch",
                    description = "Recognizable by its striped body and spiny dorsal fin",
                    habitat = "Lakes and slow-moving rivers",
                    isSelected = false
                ),
                FishType(
                    id = 9,
                    name = "Halibut",
                    description = "Largest flatfish species, prized for its firm white meat",
                    habitat = "Deep saltwater",
                    isSelected = false
                ),
                FishType(
                    id = 10,
                    name = "Grayling",
                    description = "Recognized by its large, sail-like dorsal fin",
                    habitat = "Clear, cold rivers and streams",
                    isSelected = false
                )
            )
            updateSelectedCount()
        }
    }
    
    fun toggleFishSelection(id: Int) {
        val updatedList = _fishTypes.value.map { fishType ->
            if (fishType.id == id) {
                fishType.copy(isSelected = !fishType.isSelected)
            } else {
                fishType
            }
        }
        
        _fishTypes.value = updatedList
        updateSelectedCount()
    }
    
    private fun updateSelectedCount() {
        _selectedCount.value = _fishTypes.value.count { it.isSelected }
    }
    
    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(FishSelectionViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return FishSelectionViewModel() as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}

/**
 * Data class representing a fish type
 * 
 * Note: The actual display text for these fields comes from string resources
 * in the FishSelectionScreen - these English values are just placeholders.
 */
data class FishType(
    val id: Int,
    val name: String,
    val description: String,
    val habitat: String,
    val isSelected: Boolean
) 