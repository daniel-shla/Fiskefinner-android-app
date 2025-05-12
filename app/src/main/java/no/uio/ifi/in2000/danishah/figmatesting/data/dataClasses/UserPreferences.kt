package no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses

import kotlinx.serialization.Serializable


@Serializable
data class UserPreferences(
    val name: String = "",
    val fishingLevel: String = "Ivrig fisker", // For fun, every user is "Ivrig fisker"
    
    val temperaturePreference: Int = 3,
    val windPreference: Int = 3,
    val rainPreference: Int = 3,
    val pressurePreference: Int = 3,
    val cloudPreference: Int = 3,
    
    // Time preferences
    val morningPreference: Int = 3,
    val afternoonPreference: Int = 3,
    val eveningPreference: Int = 3,
    
    // Seasonal preferences
    val springPreference: Int = 3,
    val summerPreference: Int = 3,
    val fallPreference: Int = 3,
    val winterPreference: Int = 3,
    
    val hasCompletedOnboarding: Boolean = false
) 