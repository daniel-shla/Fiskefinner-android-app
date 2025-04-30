package no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FishingLocationResponse(
    @SerialName("displayFieldName") val displayFieldName: String = "",
    @SerialName("fieldAliases") val fieldAliases: Map<String, String> = emptyMap(),
    @SerialName("geometryType") val geometryType: String = "",
    @SerialName("spatialReference") val spatialReference: Map<String, Int> = emptyMap(),
    @SerialName("fields") val fields: List<FieldInfo> = emptyList(),
    @SerialName("features") val features: List<FishingLocationFeature> = emptyList(),
    @SerialName("exceededTransferLimit") val exceededTransferLimit: Boolean = false
)

@Serializable
data class FieldInfo(
    @SerialName("name") val name: String = "",
    @SerialName("type") val type: String = "",
    @SerialName("alias") val alias: String = "",
    @SerialName("length") val length: Int = 0
)

@Serializable
data class FishingLocationFeature(
    val attributes: FishingLocationAttributes,
    val geometry: FishingLocationGeometry? = null
)

@Serializable
data class FishingLocationAttributes(
    @SerialName("stedsnavn") val name: String = "",
    @SerialName("alle_arter") val fishTypes: String? = null
)

@Serializable
data class FishingLocationGeometry(
    @SerialName("rings") val rings: List<List<List<Double>>>? = null
)

data class FishingLocation(
    val name: String,
    val fishTypes: List<String>,
    val polygonPoints: List<List<Pair<Double, Double>>>
) 