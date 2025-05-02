package no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses

import kotlinx.serialization.Serializable


@Serializable
data class UserPreferences(
    val name: String = "",
    val fishingLevel: String = "Ivrig fisker", // Store user's selected fishing level
    
    val temperaturePreference: Int = 3,    // warmt vs. kaldt
    val windPreference: Int = 3,           // lite vind vs. mye vind
    val rainPreference: Int = 3,           // tørt vs. regnete
    val pressurePreference: Int = 3,       // høyt trykk vs. lav trykk
    val cloudPreference: Int = 3,          // klar himmel vs. ææææ
    
    // Time preferences
    val morningPreference: Int = 3,        // tidlig
    val afternoonPreference: Int = 3,      // ettermiddag
    val eveningPreference: Int = 3,        // kveld
    
    // Seasonal preferences
    val springPreference: Int = 3,
    val summerPreference: Int = 3,
    val fallPreference: Int = 3,
    val winterPreference: Int = 3,
    
    val hasCompletedOnboarding: Boolean = false
) 