package no.uio.ifi.in2000.danishah.figmatesting.screens.map.components

import no.uio.ifi.in2000.danishah.figmatesting.R

fun getPinResourceForRating(rating: Int): Int {
    return when (rating) {
        4 -> R.drawable.green_pin   // Best
        3 -> R.drawable.blue_pin    // Good
        2 -> R.drawable.yellow_pin  // Fair
        1 -> R.drawable.red_pin     // Poor
        else -> R.drawable.red_pin  // Fallback
    }
}
