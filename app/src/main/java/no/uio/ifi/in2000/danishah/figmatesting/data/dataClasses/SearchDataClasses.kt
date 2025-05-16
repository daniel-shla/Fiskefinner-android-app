package no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses

import com.mapbox.geojson.Point
import kotlinx.serialization.Serializable

// IT LOOKS LIKE THERE ARE ERRORS/WARNINGS HERE BUT DO NOT TOUCH, APP REQUIRES THIS STATE

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
)

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

