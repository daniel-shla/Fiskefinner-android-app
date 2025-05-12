package no.uio.ifi.in2000.danishah.figmatesting.screens.dashboard

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.TrainingData
import no.uio.ifi.in2000.danishah.figmatesting.ml.FishPredictor


class PredictionViewModel(application: Application) : AndroidViewModel(application) {

    private val predictor = FishPredictor(application.applicationContext)

    private val _predictionText = MutableStateFlow("Laster prediksjon...")
    val predictionText: StateFlow<String> = _predictionText


    fun predictFishingConditions(input: TrainingData) {
        viewModelScope.launch {
            val inputArray = input.toInputArray()
            val prediction = predictor.predict(inputArray)

            val message = when (prediction) {
                0 -> "Lite sannsynlig med fisk"
                1 -> "Kanskje verdt det"
                2 -> "Gode fiskeforhold"
                3 -> "Drømmedag for fiske!"
                else -> "Ukjent prediksjon"
            }

            _predictionText.value = message
        }
    }


    fun predictFishingSpot(input: TrainingData): Float {
        val inputArray = input.toInputArray()
        val scores = predictor.predictScores(inputArray)

        Log.d("AI_PREDICTION", "Probabilities (score mode): ${scores.joinToString()}")

        // Returns only how probable it is with GOOD weather conditions
        return scores[2] + scores[3]
    }


    fun predictFishingClass(input: TrainingData): Int {
        val inputArray = input.toInputArray()
        return predictor.predict(inputArray)
    }

    private fun TrainingData.toInputArray(): FloatArray {
        return floatArrayOf(
            this.speciesId,
            this.temperature,
            this.windSpeed,
            this.precipitation,
            this.airPressure,
            this.cloudCover,
            this.timeOfDay,
            this.season,
            this.latitude,
            this.longitude
        )
    }
}
