package no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses

data class MittFiskeUiState(
    val locations: List<MittFiskeLocation> = emptyList(),
    val isLoaded: Boolean = false
)
