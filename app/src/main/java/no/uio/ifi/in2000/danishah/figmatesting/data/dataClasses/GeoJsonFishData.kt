package no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses

import com.google.gson.annotations.SerializedName

/**
 * Data classes for parsing GeoJSON format fish species data
 */
data class GeoJsonFishData(
    val type: String,
    val name: String,
    val crs: CoordinateReferenceSystem,
    val features: List<GeoJsonFeature>
)

data class CoordinateReferenceSystem(
    val type: String,
    val properties: CrsProperties
)

data class CrsProperties(
    val name: String
)

data class GeoJsonFeature(
    val type: String,
    val properties: GeoJsonFeatureProperties,
    val geometry: Geometry
)

data class GeoJsonFeatureProperties(
    val latinName: String
)

data class Geometry(
    val type: String,
    val coordinates: List<List<List<Double>>>
)

/**
 * Extension function to convert GeoJSON data to FishSpeciesData
 */
fun GeoJsonFishData.toFishSpeciesData(scientificName: String): FishSpeciesData {
    // Extract all polygons from features
    val polygons = features.mapNotNull { feature ->
        if (feature.geometry.type == "Polygon") {
            // Each polygon has a list of rings, we use only the outer ring (first one)
            feature.geometry.coordinates.firstOrNull()?.map { point ->
                // GeoJSON has coordinates as [longitude, latitude]
                // Our internal format uses Pair<latitude, longitude>
                if (point.size >= 2) {
                    Pair(point[1], point[0])
                } else {
                    null
                }
            }?.filterNotNull()
        } else {
            null
        }
    }
    
    return FishSpeciesData(
        scientificName = scientificName.replace(" ", "_").lowercase(),
        commonName = FishSpeciesData.getCommonName(scientificName.replace(" ", "_").lowercase()),
        polygons = polygons
    )
} 