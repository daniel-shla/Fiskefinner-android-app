package no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses
import kotlinx.serialization.Serializable
import no.uio.ifi.in2000.danishah.figmatesting.screens.dashboard.cards.FishingSpot

import com.mapbox.geojson.Point

data class Cluster(
    val center: Point,
    val spots: List<MittFiskeLocation>
)




@Serializable
data class MittFiskeLocation(
    val id: String,
    val name: String,
    val p: PointGeometry,
    val locs: List<Loc>
)

fun MittFiskeLocation.toPoint(): Point {
    val lon = p.coordinates[0]
    val lat = p.coordinates[1]
    return Point.fromLngLat(lon, lat)
}

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



