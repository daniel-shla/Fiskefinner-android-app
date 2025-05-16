package no.uio.ifi.in2000.danishah.figmatesting

import androidx.test.core.app.ApplicationProvider
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.TrainingData
import no.uio.ifi.in2000.danishah.figmatesting.screens.dashboard.PredictionViewModel
import org.junit.Assert.assertTrue
import org.junit.Test

class PredictionViewModelInstrumentedTest {

    @Test
    fun predictFishingClassReturnsValidRange() {
        val context = ApplicationProvider.getApplicationContext<android.app.Application>()
        val viewModel = PredictionViewModel(context)

        val input = TrainingData(
            speciesId = 1f,
            temperature = 10f,
            windSpeed = 2f,
            precipitation = 0f,
            airPressure = 1000f,
            cloudCover = 20f,
            timeOfDay = 12f,
            season = 2f,
            latitude = 60f,
            longitude = 10f
        )

        val result = viewModel.predictFishingClass(input)

        assertTrue(result in 0..3)
    }
}
