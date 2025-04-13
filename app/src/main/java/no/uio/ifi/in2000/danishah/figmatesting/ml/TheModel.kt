package no.uio.ifi.in2000.danishah.figmatesting.ml

import android.content.Context
import android.util.Log
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.TrainingData
import no.uio.ifi.in2000.danishah.figmatesting.data.repository.FrostRepository
import no.uio.ifi.in2000.danishah.figmatesting.data.source.FrostDataSource
import java.io.File
import kotlin.math.exp

class TheModel {

    private val inputSize = 3  // Temperatur, vindhastighet, nedbør
    private val hiddenSize = 16
    private val outputSize = 3  // Tre klasser: Dårlig, God, Helt ålreit

    private val weightsInputHidden = Array(inputSize) { FloatArray(hiddenSize) { (Math.random() * 0.2 - 0.1).toFloat() } }
    private val weightsHiddenOutput = Array(hiddenSize) { FloatArray(outputSize) { (Math.random() * 0.2 - 0.1).toFloat() } }
    private val biasHidden = FloatArray(hiddenSize) { 0f }
    private val biasOutput = FloatArray(outputSize) { 0f }

    suspend fun startTraining(context: Context) {
        val cacheFile = File(context.filesDir, "model_cache.json")
        if (!cacheFile.exists()) {
            // kopier fra assets hvis cache ikke finnes
            try {
                context.assets.open("model_cache.json").use { input ->
                    cacheFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                Log.d("TheModel", "Modell kopiert fra assets")
            } catch (e: Exception) {
                Log.e("TheModel", "Trener ny modell, kunne ikke hente modell fra assets")
                val mlDataProcessor = MLDataProcessor(FrostRepository(FrostDataSource()))
                val trainingData = mlDataProcessor.getCombinedTrainingData(context)
                train(trainingData, epochs = 100, learningRate = 0.01f)
                saveModelToFile(context)
            }
        }
        Log.d("TheModel", "Laster modell fra fil")
        loadModelFromFile(context)
    }

    private fun train(data: List<TrainingData>, epochs: Int, learningRate: Float) {
        for (epoch in 1..epochs) {
            Log.d("train", "$epoch starter")
            for (sample in data) {
                val inputs = floatArrayOf(sample.temperature, sample.windSpeed, sample.precipitation)
                val target = classifyFishingConditions(sample.fishCaught)

                // forward pass
                val hidden = FloatArray(hiddenSize)
                for (i in 0 until hiddenSize) {
                    hidden[i] = relu(inputs.indices.map { j -> inputs[j] * weightsInputHidden[j][i] }.sum() + biasHidden[i])
                }

                val output = FloatArray(outputSize)
                for (i in 0 until outputSize) {
                    output[i] = hidden.indices.map { j -> hidden[j] * weightsHiddenOutput[j][i] }.sum() + biasOutput[i]
                }

                val softmaxOutput = softmax(output)

                // backpropagation
                val outputError = FloatArray(outputSize) { i -> target[i] - softmaxOutput[i] }
                val hiddenError = FloatArray(hiddenSize) { i ->
                    outputError.sum() * weightsHiddenOutput[i].sum() * reluDerivative(hidden[i])
                }

                // Oppdater vekter
                for (i in 0 until hiddenSize) {
                    for (j in 0 until outputSize) {
                        weightsHiddenOutput[i][j] += learningRate * outputError[j] * hidden[i]
                    }
                }

                for (i in 0 until inputSize) {
                    for (j in 0 until hiddenSize) {
                        weightsInputHidden[i][j] += learningRate * hiddenError[j] * inputs[i]
                    }
                }
            }
            Log.d("train", "$epoch fullført")
        }
    }

    fun predict(input: FloatArray): Int {
        val hidden = FloatArray(hiddenSize)
        for (i in 0 until hiddenSize) {
            hidden[i] = relu(input.indices.map { j -> input[j] * weightsInputHidden[j][i] }.sum() + biasHidden[i])
        }

        val output = FloatArray(outputSize)
        for (i in 0 until outputSize) {
            output[i] = hidden.indices.map { j -> hidden[j] * weightsHiddenOutput[j][i] }.sum() + biasOutput[i]
        }

        val softmaxOutput = softmax(output)
        return softmaxOutput.indices.maxByOrNull { softmaxOutput[it] } ?: -1
    }

    private fun relu(x: Float) = if (x > 0) x else 0f
    private fun reluDerivative(x: Float) = if (x > 0) 1f else 0f

    private fun softmax(values: FloatArray): FloatArray {
        val expValues = values.map { exp(it.toDouble()).toFloat() }
        val sumExpValues = expValues.sum()
        return expValues.map { it / sumExpValues }.toFloatArray()
    }

    private fun classifyFishingConditions(fishCaught: Int): FloatArray {
        return when {
            fishCaught == 0 -> floatArrayOf(1f, 0f, 0f) // Dårlige forhold
            fishCaught in 1..2 -> floatArrayOf(0f, 1f, 0f) // Gode forhold
            else -> floatArrayOf(0f, 0f, 1f) // Helt ålreit
        }
    }

    // funksjon for å konvertere ML-modell til en CachedModel og lagre
    private fun saveModelToFile(context: Context) {
        val model = CachedModel(
            weightsInputHidden.map { it.toList() },
            weightsHiddenOutput.map { it.toList() },
            biasHidden.toList(),
            biasOutput.toList()
        )
        val json = kotlinx.serialization.json.Json.encodeToString(CachedModel.serializer(), model)
        val file = File(context.filesDir, "model_cache.json")
        file.writeText(json)
    }

    // funksjon for å laste modellen fra fil
    private fun loadModelFromFile(context: Context): Boolean {
        val file = File(context.filesDir, "model_cache.json")
        if (!file.exists()) return false

        val json = file.readText()
        val model = kotlinx.serialization.json.Json.decodeFromString(CachedModel.serializer(), json)

        // overfør data til feltene
        for (i in weightsInputHidden.indices) {
            for (j in weightsInputHidden[0].indices) {
                weightsInputHidden[i][j] = model.weightsInputHidden[i][j]
            }
        }
        for (i in weightsHiddenOutput.indices) {
            for (j in weightsHiddenOutput[0].indices) {
                weightsHiddenOutput[i][j] = model.weightsHiddenOutput[i][j]
            }
        }
        for (i in biasHidden.indices) {
            biasHidden[i] = model.biasHidden[i]
        }
        for (i in biasOutput.indices) {
            biasOutput[i] = model.biasOutput[i]
        }

        return true
    }
}
