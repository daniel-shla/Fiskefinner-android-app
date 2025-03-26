package no.uio.ifi.in2000.danishah.figmatesting.data.model

/**
 * Represents a type of fish with detailed information.
 */
data class FishType(
    val id: Int,
    val name: String,
    val description: String,
    val habitat: String,
    val isSelected: Boolean
) 