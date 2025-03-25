package no.uio.ifi.in2000.danishah.figmatesting.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class ProfileViewModel : ViewModel() {
    // User profile state
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()
    
    init {
        loadUserProfile()
    }
    
    private fun loadUserProfile() {
        viewModelScope.launch {
            // In a real app, this would load from a repository or API
            _userProfile.value = UserProfile(
                name = "Sigurd Lyckander",
                email = "sigurd.example@gmail.com",
                fishingLevel = "Expert Angler"
            )
        }
    }
    
    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return ProfileViewModel() as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}

/**
 * Data class representing a user profile
 */
data class UserProfile(
    val name: String,
    val email: String,
    val fishingLevel: String
) 