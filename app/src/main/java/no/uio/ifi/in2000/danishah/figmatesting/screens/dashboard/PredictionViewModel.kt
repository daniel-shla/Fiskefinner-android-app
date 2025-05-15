package no.uio.ifi.in2000.danishah.figmatesting.screens.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.TrainingData
import no.uio.ifi.in2000.danishah.figmatesting.ml.FishPredictor


class PredictionViewModel(application: Application) : AndroidViewModel(application) {

    private val predictor = FishPredictor(application.applicationContext)

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
