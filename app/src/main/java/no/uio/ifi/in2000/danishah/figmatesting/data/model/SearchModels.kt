package no.uio.ifi.in2000.danishah.figmatesting.data.model

import com.mapbox.geojson.Point
import kotlinx.serialization.Serializable

// DET SER UT SOM AT DET ER MYE SOM ER MASSE FEIL HER, MEN ALT ER RIKTIG
// IKKE ENDRE NOE

@Serializable
data class SearchSuggestion(
    val name: String,
    val mapboxId: String,
    val featureType: String,
    val address: String? = null,
    val fullAddress: String? = null,
    val placeFormatted: String? = null,
    val maki: String? = null
)

@Serializable
data class SearchResponse(
    val suggestions: List<SearchSuggestion>,
    val attribution: String
)

@Serializable
data class SearchFeature(
    val name: String,
    val coordinates: PointDto,
    val address: String? = null,
    val fullAddress: String? = null,
    val featureType: String
)

@Serializable
data class PointDto(
    val longitude: Double,
    val latitude: Double
) {
    fun toPoint(): Point = Point.fromLngLat(longitude, latitude)
}

@Serializable
data class FeatureGeometry(
    val coordinates: List<Double>,
    val type: String = "Point"
)

@Serializable
data class FeatureProperties(
    val name: String,
    val mapbox_id: String,
    val feature_type: String,
    val address: String? = null,
    val full_address: String? = null,
    val place_formatted: String? = null,
    val coordinates: FeatureCoordinates
)

@Serializable
data class FeatureCoordinates(
    val longitude: Double,
    val latitude: Double
)

@Serializable
data class Feature(
    val type: String,
    val geometry: FeatureGeometry,
    val properties: FeatureProperties
)

@Serializable
data class RetrieveResponse(
    val type: String,
    val features: List<Feature>,
    val attribution: String
) {
    fun toSearchFeature(): SearchFeature? {
        if (features.isEmpty()) return null
        
        val feature = features[0]
        val properties = feature.properties
        return SearchFeature(
            name = properties.name,
            coordinates = PointDto(properties.coordinates.longitude, properties.coordinates.latitude),
            address = properties.address,
            fullAddress = properties.full_address,
            featureType = properties.feature_type
        )
    }
} 