package no.uio.ifi.in2000.danishah.figmatesting.ml

import kotlinx.serialization.Serializable

// DATAKLASSE FOR Å LAGRE ML-MODELLEN SOM EN FIL
// så kanskje vi kan unngå å trene modellen HVER gang appen startes :')
@Serializable
data class CachedModel(
    val weightsInputHidden: List<List<Float>>,
    val weightsHiddenOutput: List<List<Float>>,
    val biasHidden: List<Float>,
    val biasOutput: List<Float>
)