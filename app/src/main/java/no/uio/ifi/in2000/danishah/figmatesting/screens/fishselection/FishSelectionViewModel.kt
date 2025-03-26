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
                    name = "Atlantisk laks",
                    description = "Vanlig i norske elver, kjent for sin hoppevne",
                    habitat = "Elver og kystområder",
                    isSelected = false
                ),
                FishType(
                    id = 2,
                    name = "Ørret",
                    description = "Populær ferskvannsfisk med karakteristisk prikkete mønster",
                    habitat = "Elver, innsjøer og bekker",
                    isSelected = false
                ),
                FishType(
                    id = 3,
                    name = "Røye",
                    description = "Kaldtvannsart som finnes i fjellvann og elver",
                    habitat = "Dype, kalde fjellvann",
                    isSelected = false
                ),
                FishType(
                    id = 4,
                    name = "Torsk",
                    description = "Norges viktigste kommersielle fisk",
                    habitat = "Saltvann, kystområder",
                    isSelected = false
                ),
                FishType(
                    id = 5,
                    name = "Makrell",
                    description = "Hurtigsvømmende pelagisk fisk, utmerket for nybegynnere",
                    habitat = "Kyst- og åpne havområder",
                    isSelected = false
                ),
                FishType(
                    id = 6,
                    name = "Sei",
                    description = "Vanlig kystfisk, bra for nybegynnere",
                    habitat = "Kystvann",
                    isSelected = false
                ),
                FishType(
                    id = 7,
                    name = "Gjedde",
                    description = "Aggressiv rovfisk med skarpe tenner",
                    habitat = "Innsjøer og sakteflytende elver",
                    isSelected = false
                ),
                FishType(
                    id = 8,
                    name = "Abbor",
                    description = "Gjenkjennelig med sin stripete kropp og piggete ryggfinne",
                    habitat = "Innsjøer og sakteflytende elver",
                    isSelected = false
                ),
                FishType(
                    id = 9,
                    name = "Kveite",
                    description = "Største flatfiskart, verdsatt for sitt faste hvite kjøtt",
                    habitat = "Dypt saltvann",
                    isSelected = false
                ),
                FishType(
                    id = 10,
                    name = "Harr",
                    description = "Gjenkjennes på sin store, seilaktige ryggfinne",
                    habitat = "Klare, kalde elver og bekker",
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

// placeholder data, dummy no api
data class FishType(
    val id: Int,
    val name: String,
    val description: String,
    val habitat: String,
    val isSelected: Boolean
) 