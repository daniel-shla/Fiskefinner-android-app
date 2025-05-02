package no.uio.ifi.in2000.danishah.figmatesting.screens.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.UserPreferences
import no.uio.ifi.in2000.danishah.figmatesting.data.repository.UserPreferencesRepository

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = UserPreferencesRepository(application.applicationContext)
    
    // User profile state
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()
    
    // User preferences state
    private val _userPreferences = MutableStateFlow(UserPreferences())
    val userPreferences: StateFlow<UserPreferences> = _userPreferences.asStateFlow()
    
    init {
        loadUserProfile()
        loadUserPreferences()
    }
    
    private fun loadUserProfile() {
        viewModelScope.launch {
            _userProfile.value = UserProfile(
                name = _userPreferences.value.name.ifBlank { "Anonym fisker" },
                email = "exempel@fiskefinne.no",
                fishingLevel = "Ivrig fisker"
            )
        }
    }
    
    private fun loadUserPreferences() {
        viewModelScope.launch {
            repository.userPreferencesFlow.collect { preferences ->
                _userPreferences.value = preferences
                
                _userProfile.value = _userProfile.value?.copy(
                    name = preferences.name.ifBlank { "Anonym fisker" }
                ) ?: UserProfile(
                    name = preferences.name.ifBlank { "Anonym fisker" },
                    email = "exempel@fiskefinner.no",
                    fishingLevel = "Ivrig fisker"
                )
            }
        }
    }
    

    suspend fun logout(): Boolean {
        repository.logout()
        return true
    }
    
    companion object {
        class Factory(private val application: Application) : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return ProfileViewModel(application) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}

data class UserProfile(
    val name: String,
    val email: String,
    val fishingLevel: String
) 