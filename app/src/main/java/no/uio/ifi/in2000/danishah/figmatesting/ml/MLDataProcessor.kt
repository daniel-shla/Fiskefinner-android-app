package no.uio.ifi.in2000.danishah.figmatesting.ml

import android.content.Context
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.TrainingData
import no.uio.ifi.in2000.danishah.figmatesting.data.repository.FrostRepository
import no.uio.ifi.in2000.danishah.figmatesting.data.source.FrostDataSource
import java.io.BufferedReader
import java.io.InputStreamReader

class MLDataProcessor(private val frostRepository: FrostRepository) {
    // Funksjon for å lese CSV-filen
    fun readCsvFromAssets(context: Context, fileName: String): List<TrainingData> {
        val trainingDataList = mutableListOf<TrainingData>()

        try {
            val inputStream = context.assets.open(fileName)
            val reader = BufferedReader(InputStreamReader(inputStream))

            // skippe header-linjen
            reader.readLine()

            reader.forEachLine { line ->
                val tokens = line.split(",")
                if (tokens.size == 6) {
                    val temperature = tokens[2].toFloatOrNull() ?: 0f
                    val windSpeed = tokens[3].toFloatOrNull() ?: 0f
                    val precipitation = tokens[4].toFloatOrNull() ?: 0f
                    val fishCaught = tokens[5].toIntOrNull() ?: 0

                    trainingDataList.add(TrainingData(temperature, windSpeed, precipitation, fishCaught))
                }
            }
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return trainingDataList
    }

    // Funksjon for å kombinere Frost API-data med CSV-data
    suspend fun getCombinedTrainingData(context: Context): List<TrainingData> {
        val frostRepository = FrostRepository(FrostDataSource())

        // Hent værdata fra Frost API
        // val frostResponse = frostRepository.getFrostData()
        val frostTrainingData = prepareTrainingData()

        // Hent syntetiske data fra CSV
        val csvTrainingData = readCsvFromAssets(context, "synthetic_fish_data.csv")

        // Kombiner datasettene
        return frostTrainingData + csvTrainingData
    }

    private suspend fun prepareTrainingData(): List<TrainingData> {
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

            // enkle regler for fiskefangst basert på været
            fishCaught = if (temperature > 10 && windSpeed < 5 && precipitation < 3) {
                (1..5).random()
            } else {
                0
            }

            dataList.add(TrainingData(temperature.toFloat(), windSpeed.toFloat(), precipitation.toFloat(), fishCaught))
        }

        return dataList
    }

    fun process(temperature: Double, windSpeed: Double, precipitation: Double): FloatArray {
        // Normaliser verdier (juster skala basert på treningsdataene!!!)
        val normTemp = (temperature / 40.0).toFloat() // Anta maks temp = 40°C
        val normWind = (windSpeed / 20.0).toFloat()  // Anta maks vind = 20 m/s
        val normPrec = (precipitation / 10.0).toFloat() // Anta maks nedbør = 10 mm

        return floatArrayOf(normTemp, normWind, normPrec)
    }

}

