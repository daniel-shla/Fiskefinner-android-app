package no.uio.ifi.in2000.danishah.figmatesting.ml

import android.content.Context
import android.util.Log
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.TrainingData
import no.uio.ifi.in2000.danishah.figmatesting.data.repository.FrostRepository
import no.uio.ifi.in2000.danishah.figmatesting.data.source.FrostDataSource
import java.io.BufferedReader
import java.io.InputStreamReader

class MLDataProcessor(private val frostRepository: FrostRepository) {
    // funksjon for å lese CSV-filen
    private fun readCsvFromAssets(context: Context, fileName: String): List<TrainingData> {
        return try {
            Log.d("MLDataProcessor", "Prøver å åpne fra cache")
            context.assets.open(fileName).bufferedReader().useLines { lines ->
                lines.drop(1).mapNotNull { line ->
                    val tokens = line.split(",")
                    try {
                        Log.d("MLDataProcessor", "Setter TrainingData fra tokens")
                        TrainingData(
                            temperature = tokens[0].toFloatOrNull() ?: 0f,
                            windSpeed = tokens[1].toFloatOrNull() ?: 0f,
                            precipitation = tokens[2].toFloatOrNull() ?: 0f,
                            airPressure = tokens[3].toFloatOrNull() ?: 0f,
                            cloudCover = tokens[4].toFloatOrNull() ?: 0f,
                            timeOfDay = tokens[5].toFloatOrNull() ?: 0f,
                            season = tokens[6].toFloatOrNull() ?: 0f,
                            latitude = tokens[7].toFloatOrNull() ?: 0f,
                            longitude = tokens[8].toFloatOrNull() ?: 0f,
                            fishCaught = tokens[9].toIntOrNull() ?: 0
                        )
                    } catch (e: Exception) {
                        Log.e("MLDataProcessor", "Feil i linje: $line", e)
                        null
                    }
                }.toList()
            }
        } catch (e: Exception) {
            Log.e("MLDataProcessor", "Kunne ikke lese CSV", e)
            emptyList()
        }
    }

    // Funksjon for å kombinere Frost API-data med CSV-data
    suspend fun getCombinedTrainingData(context: Context): List<TrainingData> {
        val frostRepository = FrostRepository(FrostDataSource())

        // Hent værdata fra Frost API
        // val frostResponse = frostRepository.getFrostData()
        val frostTrainingData = prepareTrainingData()

        // Hent syntetiske data fra CSV
        val csvTrainingData = readCsvFromAssets(context, "synthetic_fish_data_SMALL.csv")

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
            var airPressure = 1000.0 // placeholder
            var cloudCover = 50.0 // placeholder
            var fishCaught = 0

            data.observations.forEach { observation ->
                when (observation.elementId) {
                    "air_temperature" -> temperature = observation.value
                    "wind_speed" -> windSpeed = observation.value
                    "precipitation_amount" -> precipitation = observation.value
                    // "air_pressure_at_sea_level" -> airPressure = observation.value
                    // "cloud_area_fraction" -> cloudCover = observation.value
                }
            }

            // simulere litt ekstra info
            val timeOfDay = (0..23).random().toFloat()
            val season = when {
                temperature < 0 -> 0f
                temperature < 8 -> 1f
                temperature < 18 -> 3f
                else -> 2f
            }
            val latitude = 60.0f
            val longitude = 10.0f

            // enkle regler for fiskefangst basert på været
            fishCaught = if (temperature > 10 && windSpeed < 5 && precipitation < 3) {
                (1..5).random()
            } else {
                0
            }

            dataList.add(
                TrainingData(
                    temperature.toFloat(),
                    windSpeed.toFloat(),
                    precipitation.toFloat(),
                    airPressure.toFloat(),
                    cloudCover.toFloat(),
                    timeOfDay,
                    season,
                    latitude,
                    longitude,
                    fishCaught
                )
            )
        }

        return dataList
    }

    fun process(
        temperature: Double,
        windSpeed: Double,
        precipitation: Double,
        airPressure: Double,
        cloudCover: Double,
        timeOfDay: Double,
        season: Double,
        latitude: Double,
        longitude: Double
    ): FloatArray {
        return floatArrayOf(
            (temperature/40.0).toFloat(), // -10 til 30 grader
            (windSpeed/20.0).toFloat(), // 0 til 20 m/s
            (precipitation / 10.0).toFloat(), // 0 til 10 mm
            ((airPressure - 950) / 100).toFloat(), // 950-1050 hPa
            (cloudCover / 100.0).toFloat(), // %
            (timeOfDay / 23.0).toFloat(), // 0-23 -> 0-1
            (season / 3.0).toFloat(), // 0..3
            ((latitude - 59.0) / 5.0).toFloat(), // 59–64 grader
            ((longitude - 5.0) / 7.0).toFloat() // 5–12 grader
        )
    }

    // funksjoner for å normalisere data
    // gjør trening mer effektivt og stabilt
    fun normalizeTemperature(t: Float) = (t + 30f) / 60f // antar -30 til +30
    fun normalizeWindSpeed(w: Float) = w / 20f // anta 0–20 m/s
    fun normalizePrecipitation(p: Float) = p / 10f // anta 0–10 mm
    fun normalizeAirPressure(p: Float) = (p - 950f) / 100f // anta 950–1050 hPa
    fun normalizeCloudCover(c: Float) = c / 100f // 0–100 %
    fun normalizeTimeOfDay(t: Float) = t / 23f // 0–23 timer
    fun normalizeSeason(s: Float) = s / 3f // 0 = vinter, 1 = vår, 2 = sommer, 3 = høst
    fun normalizeLatitude(lat: Float) = (lat - 59f) / 5f // Norge: ca 59–64
    fun normalizeLongitude(lon: Float) = (lon - 5f) / 7f // Østlandet! utvide evt men forskjellen blir ikke såå mye
}