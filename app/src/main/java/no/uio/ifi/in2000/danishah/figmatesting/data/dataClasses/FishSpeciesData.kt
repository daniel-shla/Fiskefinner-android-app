package no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses

/**
 * Represents fish species data for display on map.
 */
data class FishSpeciesData(
    val scientificName: String,
    val commonName: String,
    val polygons: List<List<Pair<Double, Double>>>
) {
    companion object {
        // Map of scientific names to common Norwegian names
        val COMMON_NAMES = mapOf(
            "gadus_morhua" to "Torsk",
            "melanogrammus_aeglefinus" to "Hyse",
            "pollachius_virens" to "Sei",
            "scomber_scombrus" to "Makrell",
            "pleuronectes_platessa" to "Rødspette",
            "hippoglossus_hippoglossus" to "Kveite",
            "dicentrarchus_labrax" to "Havabbor",
            "anarhichas_lupus" to "Steinbit",
            "esox_lucius" to "Gjedde",
            "salmo_salar" to "Laks",
            "salvelinus_alpinus" to "Røye",
            "perca_fluviatilis" to "Abbor"
        )

        // Get common name from scientific name
        fun getCommonName(scientificName: String): String {
            val key = scientificName.lowercase().replace(" ", "_")
            return COMMON_NAMES[key] ?: scientificName.split("_")
                .joinToString(" ") { it.capitalize() }
        }
    }
}

// Extension function to capitalize the first letter of a string
private fun String.capitalize(): String {
    return if (this.isEmpty()) this
    else this[0].uppercase() + this.substring(1)
} 