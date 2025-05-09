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

/**
 * ViewModel som kobler UI mot den underliggende ML-modellen via FishPredictor.
 * Håndterer både score-basert sannsynlighet og klasseprediksjon.
 */
class PredictionViewModel(application: Application) : AndroidViewModel(application) {

    private val predictor = FishPredictor(application.applicationContext)

    // UI state for visning i tekst (ikke nødvendig hvis du kun bruker score internt)
    private val _predictionText = MutableStateFlow("Laster prediksjon...")
    val predictionText: StateFlow<String> = _predictionText

    /**
     * Predikerer fiske med klasser 0–3 og viser resultat som tekst.
     * Brukes for å gi brukerforståelig tilbakemelding, f.eks. "Gode fiskeforhold".
     */
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

    /**
     * Returnerer sannsynlighetsscore for "gode forhold" – altså summen av klasse 2 og 3.
     * Brukes for å beregne en kontinuerlig rating som kan kartes til 1–5.
     */
    fun predictFishingSpot(input: TrainingData): Float {
        val inputArray = input.toInputArray()
        val scores = predictor.predictScores(inputArray)

        Log.d("AI_PREDICTION", "Probabilities (score mode): ${scores.joinToString()}")

        // Returnerer kun hvor sannsynlig det er med gode forhold
        return scores[2] + scores[3]
    }

    /**
     * Returnerer predikert klasse direkte (0–3).
     * Brukes når du ønsker å knytte en pin eller visuell indikator til en bestemt klasse.
     */
    fun predictFishingClass(input: TrainingData): Int {
        val inputArray = input.toInputArray()
        return predictor.predict(inputArray)
    }

    /**
     * Hjelpefunksjon for å gjøre om treningsdata til float-array for modellen.
     */
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
