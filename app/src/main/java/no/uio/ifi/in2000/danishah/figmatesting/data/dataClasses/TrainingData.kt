package no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses

data class TrainingData(
    val temperature: Float,
    val windSpeed: Float,
    val precipitation: Float,
    val airPressure: Float,
    val cloudCover: Float,
    val timeOfDay: Float,
    val season: Float,
    val latitude: Float,
    val longitude: Float,
    val fishCaught: Int,
    val speciesId: Float
)
