package no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class FrostResponse(
    val data: List<FrostData>
)

@Serializable
data class FrostData(
    val sourceId: String,
    val referenceTime: String,
    val observations: List<Observation>
)

@Serializable
data class Observation(
    val elementId: String,
    val value: Double,
    val unit: String
)