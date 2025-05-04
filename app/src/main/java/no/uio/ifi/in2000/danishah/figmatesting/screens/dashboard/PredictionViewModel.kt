package no.uio.ifi.in2000.danishah.figmatesting.screens.dashboard

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.TrainingData
import no.uio.ifi.in2000.danishah.figmatesting.data.repository.FrostRepository
import no.uio.ifi.in2000.danishah.figmatesting.data.source.FrostDataSource
import no.uio.ifi.in2000.danishah.figmatesting.ml.MLDataProcessor
import no.uio.ifi.in2000.danishah.figmatesting.ml.TheModel

class PredictionViewModel(application: Application) : AndroidViewModel(application) {
    private val model = TheModel()

    private val _predictionText = MutableStateFlow("Laster prediksjon...")
    val predictionText: StateFlow<String> = _predictionText

    init { // trene ML-modellen
        viewModelScope.launch {
            model.startTraining(getApplication<Application>().applicationContext)
        }
    }

    /*
    fun predictFishingConditions(
        temp: Float,
        wind: Float,
        precipitation: Float,
        pressure: Float,
        cloudCover: Float,
        timeOfDay: Float,
        season: Float,
        latitude: Float,
        longitude: Float
    ) {
     */
    fun predictFishingConditions(input: TrainingData) {
        Log.d("AI model", "Trainging data: $input")
        viewModelScope.launch {
            val processor = MLDataProcessor(FrostRepository(FrostDataSource())) // eller refaktorer så du ikke trenger repo her
            val normalizedInput = floatArrayOf(
                processor.normalizeTemperature(input.temperature),
                processor.normalizeWindSpeed(input.windSpeed),
                processor.normalizePrecipitation(input.precipitation),
                processor.normalizeAirPressure(input.airPressure),
                processor.normalizeCloudCover(input.cloudCover),
                processor.normalizeTimeOfDay(input.timeOfDay),
                processor.normalizeSeason(input.season),
                processor.normalizeLatitude(input.latitude),
                processor.normalizeLongitude(input.longitude)
            )
            val prediction = model.predict(normalizedInput)

            val probability = when {
                prediction >= 0.7f -> "God dag for fiske!"
                prediction >= 0.4f -> "Ok dag for fiske."
                else -> "Fiskeforholdene er ikke på topp i dag."
            }
            /*
            _predictionText.value = when (prediction) {
                0 -> "Dårlig dag for fiske"
                1 -> "God dag for fiske!"
                2 -> "Helt ålreit dag for fiske"
                else -> "Ukjent prediksjon"
            }*/
            _predictionText.value = probability
        }
    }

    suspend fun predictFishingSpot(input: TrainingData): Float {
        val processor = MLDataProcessor(FrostRepository(FrostDataSource()))

        val normalizedInput = floatArrayOf(
            processor.normalizeTemperature(input.temperature),
            processor.normalizeWindSpeed(input.windSpeed),
            processor.normalizePrecipitation(input.precipitation),
            processor.normalizeAirPressure(input.airPressure),
            processor.normalizeCloudCover(input.cloudCover),
            processor.normalizeTimeOfDay(input.timeOfDay),
            processor.normalizeSeason(input.season),
            processor.normalizeLatitude(input.latitude),
            processor.normalizeLongitude(input.longitude)
        )

        return model.predict(normalizedInput)
    }
}