package no.uio.ifi.in2000.danishah.figmatesting.screens.onboarding

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
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
import no.uio.ifi.in2000.danishah.figmatesting.screens.dashboard.LoactionForecast.WeatherViewModel
import no.uio.ifi.in2000.danishah.figmatesting.screens.dashboard.PredictionViewModel
import no.uio.ifi.in2000.danishah.figmatesting.screens.map.mittFiske.MittFiskeViewModel


class OnboardingViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = UserPreferencesRepository(application.applicationContext)
    
    private val _uiState = MutableStateFlow(UserPreferences())
    val uiState: StateFlow<UserPreferences> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.userPreferencesFlow.collect { preferences ->
                _uiState.value = preferences
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun preloadFishLocations(
        mittFiskeViewModel: MittFiskeViewModel,
        weatherViewModel: WeatherViewModel,
        predictionViewModel: PredictionViewModel,
        polygonWKT: String,
        pointWKT: String,
        species: String,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            mittFiskeViewModel.loadLocations(
                polygonWKT = polygonWKT,
                pointWKT = pointWKT,
                weatherViewModel = weatherViewModel,
                predictionViewModel = predictionViewModel,
                selectedSpecies = species,
                onDone = onDone
            )
        }
    }


    //THE AMOUNT OF BOILERPLATE RREEEEEEEEEEE
    // love that for us <3

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    fun updateTemperaturePreference(value: Int) {
        _uiState.value = _uiState.value.copy(temperaturePreference = value)
    }
    
    fun updateWindPreference(value: Int) {
        _uiState.value = _uiState.value.copy(windPreference = value)
    }
    
    fun updateRainPreference(value: Int) {
        _uiState.value = _uiState.value.copy(rainPreference = value)
    }
    
    fun updatePressurePreference(value: Int) {
        _uiState.value = _uiState.value.copy(pressurePreference = value)
    }
    
    fun updateCloudPreference(value: Int) {
        _uiState.value = _uiState.value.copy(cloudPreference = value)
    }
    
    fun updateMorningPreference(value: Int) {
        _uiState.value = _uiState.value.copy(morningPreference = value)
    }
    
    fun updateAfternoonPreference(value: Int) {
        _uiState.value = _uiState.value.copy(afternoonPreference = value)
    }
    
    fun updateEveningPreference(value: Int) {
        _uiState.value = _uiState.value.copy(eveningPreference = value)
    }
    
    fun updateSpringPreference(value: Int) {
        _uiState.value = _uiState.value.copy(springPreference = value)
    }
    
    fun updateSummerPreference(value: Int) {
        _uiState.value = _uiState.value.copy(summerPreference = value)
    }
    
    fun updateFallPreference(value: Int) {
        _uiState.value = _uiState.value.copy(fallPreference = value)
    }
    
    fun updateWinterPreference(value: Int) {
        _uiState.value = _uiState.value.copy(winterPreference = value)
    }
    
    // Save all
    fun savePreferences() {
        viewModelScope.launch {
            repository.updateUserPreferences(
                _uiState.value.copy(hasCompletedOnboarding = true)
            )
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(OnboardingViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return OnboardingViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 