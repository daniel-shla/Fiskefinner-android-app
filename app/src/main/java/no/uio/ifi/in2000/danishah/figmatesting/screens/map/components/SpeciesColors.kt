package no.uio.ifi.in2000.danishah.figmatesting.screens.map.components

import androidx.compose.ui.graphics.Color


fun getColorForSpecies(scientificName: String): Color {
    // Map of species to specific colors
    val speciesColors = mapOf(
        "gadus_morhua" to Color(0xFF0277BD),           // Dark blue
        "melanogrammus_aeglefinus" to Color(0xFF558B2F), // Green
        "pollachius_virens" to Color(0xFFF57F17),      // Yellow/orange
        "scomber_scombrus" to Color(0xFF6A1B9A),       // Purple
        "pleuronectes_platessa" to Color(0xFFD81B60),  // Pink
        "hippoglossus_hippoglossus" to Color(0xFF1B5E20), // Dark green
        "dicentrarchus_labrax" to Color(0xFF4E342E),   // Brown 
        "anarhichas_lupus" to Color(0xFFE64A19),       // Orange
        "esox_lucius" to Color(0xFF0097A7),            // Teal
        "salmo_salar" to Color(0xFFEF5350),            // Red 
        "salvelinus_alpinus" to Color(0xFF7E57C2),     // Light purple
        "perca_fluviatilis" to Color(0xFF689F38)       // Light green
    )
    
    // Return the specific color for this species or a default if not found
    return speciesColors[scientificName] ?: Color(0xFFFF0000) // Default to red
} 