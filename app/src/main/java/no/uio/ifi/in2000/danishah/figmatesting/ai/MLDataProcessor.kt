package no.uio.ifi.in2000.danishah.figmatesting.ai

import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.TrainingData
import no.uio.ifi.in2000.danishah.figmatesting.data.repository.FrostRepository

class MLDataProcessor(private val frostRepository: FrostRepository) {

    suspend fun prepareTrainingData(): List<TrainingData> {
        val frostData = frostRepository.getFrostData()
        val dataList = mutableListOf<TrainingData>()

        frostData.data.forEach { data ->
            var temperature = 0.0
            var windSpeed = 0.0
            var precipitation = 0.0
            var fishCaught = 0 // Simulert fiskefangst

            data.observations.forEach { observation ->
                when (observation.elementId) {
                    "air_temperature" -> temperature = observation.value
                    "wind_speed" -> windSpeed = observation.value
                    "precipitation_amount" -> precipitation = observation.value
                }
            }

            // Enkle regler for fiskefangst basert på været
            fishCaught = if (temperature > 10 && windSpeed < 5 && precipitation < 3) {
                (1..5).random()
            } else {
                0
            }

            dataList.add(TrainingData(temperature.toFloat(), windSpeed.toFloat(), precipitation.toFloat(), fishCaught))
        }

        return dataList
    }
}

