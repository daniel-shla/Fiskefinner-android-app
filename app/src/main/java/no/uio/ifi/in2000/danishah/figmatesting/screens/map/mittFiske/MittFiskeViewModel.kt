package no.uio.ifi.in2000.danishah.figmatesting.screens.map.mittFiske

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.MittFiskeLocation
import no.uio.ifi.in2000.danishah.figmatesting.data.repository.MittFiskeRepository

data class MittFiskeUiState(
    val locations: List<MittFiskeLocation> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class MittFiskeViewModel(
    private val repository: MittFiskeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MittFiskeUiState())
    val uiState: StateFlow<MittFiskeUiState> = _uiState

    fun loadLocations(
        polygonWKT: String,
        pointWKT: String,
        min: Int = 13,
        max: Int = 20

    ) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            repository.getLocationsForArea(polygonWKT, pointWKT, min, max)
                .onSuccess { locations ->
                    _uiState.update {
                        it.copy(
                            locations = locations,
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "Ukjent feil"
                        )
                    }
                }
        }
    }
}
