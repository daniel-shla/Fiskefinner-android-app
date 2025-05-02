package no.uio.ifi.in2000.danishah.figmatesting.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.UserPreferences

class UserPreferencesRepository(private val context: Context) {
    
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")
        
        private val USER_NAME = stringPreferencesKey("user_name")
        private val FISHING_LEVEL = stringPreferencesKey("fishing_level")
        private val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
        
        private val TEMPERATURE_PREFERENCE = intPreferencesKey("temperature_preference")
        private val WIND_PREFERENCE = intPreferencesKey("wind_preference")
        private val RAIN_PREFERENCE = intPreferencesKey("rain_preference")
        private val PRESSURE_PREFERENCE = intPreferencesKey("pressure_preference")
        private val CLOUD_PREFERENCE = intPreferencesKey("cloud_preference")
        
        private val MORNING_PREFERENCE = intPreferencesKey("morning_preference")
        private val AFTERNOON_PREFERENCE = intPreferencesKey("afternoon_preference")
        private val EVENING_PREFERENCE = intPreferencesKey("evening_preference")
        
        private val SPRING_PREFERENCE = intPreferencesKey("spring_preference")
        private val SUMMER_PREFERENCE = intPreferencesKey("summer_preference")
        private val FALL_PREFERENCE = intPreferencesKey("fall_preference")
        private val WINTER_PREFERENCE = intPreferencesKey("winter_preference")
    }
    
    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { preferences ->
        UserPreferences(
            name = preferences[USER_NAME] ?: "",
            fishingLevel = preferences[FISHING_LEVEL] ?: "Ivrig fisker",
            
            temperaturePreference = preferences[TEMPERATURE_PREFERENCE] ?: 3,
            windPreference = preferences[WIND_PREFERENCE] ?: 3,
            rainPreference = preferences[RAIN_PREFERENCE] ?: 3,
            pressurePreference = preferences[PRESSURE_PREFERENCE] ?: 3,
            cloudPreference = preferences[CLOUD_PREFERENCE] ?: 3,
            
            morningPreference = preferences[MORNING_PREFERENCE] ?: 3,
            afternoonPreference = preferences[AFTERNOON_PREFERENCE] ?: 3,
            eveningPreference = preferences[EVENING_PREFERENCE] ?: 3,
            
            springPreference = preferences[SPRING_PREFERENCE] ?: 3,
            summerPreference = preferences[SUMMER_PREFERENCE] ?: 3,
            fallPreference = preferences[FALL_PREFERENCE] ?: 3,
            winterPreference = preferences[WINTER_PREFERENCE] ?: 3,
            
            hasCompletedOnboarding = preferences[HAS_COMPLETED_ONBOARDING] ?: false
        )
    }
    
    suspend fun updateUserPreferences(userPreferences: UserPreferences) {
        context.dataStore.edit { preferences ->
            preferences[USER_NAME] = userPreferences.name
            preferences[FISHING_LEVEL] = userPreferences.fishingLevel
            preferences[HAS_COMPLETED_ONBOARDING] = userPreferences.hasCompletedOnboarding
            
            preferences[TEMPERATURE_PREFERENCE] = userPreferences.temperaturePreference
            preferences[WIND_PREFERENCE] = userPreferences.windPreference
            preferences[RAIN_PREFERENCE] = userPreferences.rainPreference
            preferences[PRESSURE_PREFERENCE] = userPreferences.pressurePreference
            preferences[CLOUD_PREFERENCE] = userPreferences.cloudPreference
            
            preferences[MORNING_PREFERENCE] = userPreferences.morningPreference
            preferences[AFTERNOON_PREFERENCE] = userPreferences.afternoonPreference
            preferences[EVENING_PREFERENCE] = userPreferences.eveningPreference
            
            preferences[SPRING_PREFERENCE] = userPreferences.springPreference
            preferences[SUMMER_PREFERENCE] = userPreferences.summerPreference
            preferences[FALL_PREFERENCE] = userPreferences.fallPreference
            preferences[WINTER_PREFERENCE] = userPreferences.winterPreference
        }
    }
    
    suspend fun setOnboardingCompleted() {
        context.dataStore.edit { preferences ->
            preferences[HAS_COMPLETED_ONBOARDING] = true
        }
    }
    
    val hasCompletedOnboarding: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[HAS_COMPLETED_ONBOARDING] ?: false
    }
    
    suspend fun logout() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}