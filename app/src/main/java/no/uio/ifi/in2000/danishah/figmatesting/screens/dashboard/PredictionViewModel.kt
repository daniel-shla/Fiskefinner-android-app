package no.uio.ifi.in2000.danishah.figmatesting.screens.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    fun predictFishingConditions(temp: Float, wind: Float, precipitation: Float) {
        viewModelScope.launch {
            val processor = MLDataProcessor(FrostRepository(FrostDataSource())) // eller refaktorer så du ikke trenger repo her
            val input = processor.process(temp.toDouble(), wind.toDouble(), precipitation.toDouble())
            val prediction = model.predict(input)

            _predictionText.value = when (prediction) {
                0 -> "Dårlig dag for fiske"
                1 -> "God dag for fiske!"
                2 -> "Helt ålreit dag for fiske"
                else -> "Ukjent prediksjon"
            }
        }
    }

    suspend fun predictFishingSpotConditions(
        temp: Float,
        wind: Float,
        precipitation: Float
    ): Int = withContext(Dispatchers.IO) {
        val processor = MLDataProcessor(FrostRepository(FrostDataSource()))
        val input = processor.process(temp.toDouble(), wind.toDouble(), precipitation.toDouble())
        val prediction = model.predict(input)
        prediction
    }


    /*
    fun predictFishingConditions(temp: Float, wind: Float, precipitation: Float) {
        viewModelScope.launch {
            val prediction = model.predict(floatArrayOf(temp, wind, precipitation))
            _predictionText.value = when (prediction) {
                0 -> "Dårlig dag for fiske"
                1 -> "God dag for fiske!"
                2 -> "Helt ålreit dag for fiske"
                else -> "Ukjent prediksjon"
            }
        }
    }

     */
}
