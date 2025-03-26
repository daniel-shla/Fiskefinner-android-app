package no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses
import kotlinx.serialization.Serializable

@Serializable
data class MittFiskeLocation(
    val id: String,
    val name: String,
    val p: PointGeometry,
    val locs: List<Loc>
)

@Serializable
data class PointGeometry(
    val type: String,
    val coordinates: List<Double>
)

@Serializable
data class Loc(
    val ca: String,
    val n: String,
    val i: String?,
    val u: String,
    val k: String,
    val f: String,
    val fe: List<String>?,
    val de: String
)
