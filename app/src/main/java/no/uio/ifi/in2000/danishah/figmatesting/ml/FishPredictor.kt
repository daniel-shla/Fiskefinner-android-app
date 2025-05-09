package no.uio.ifi.in2000.danishah.figmatesting.ml

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.json.JSONObject
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

/**
 * Ansvarlig for å kjøre prediksjoner mot en TFLite-modell.
 * Bruker 10 input-funksjoner, skalert med z-score (mean og scale fra scaler_params.json).
 * Returnerer enten klasse (0–3) eller score (sannsynligheter for hver klasse).
 */
class FishPredictor(context: Context) {
    private val interpreter: Interpreter
    private val mean: FloatArray
    private val scale: FloatArray

    init {
        // Last inn TFLite-modellen fra assets
        val model = loadModelFile(context, "fish_model.tflite")
        interpreter = Interpreter(model)

        // Last inn scaler-parametere (mean og scale for normalisering)
        val scalerJson = context.assets.open("scaler_params.json")
            .bufferedReader().use { it.readText() }

        val obj = JSONObject(scalerJson)
        mean = obj.getJSONArray("mean").let { arr ->
            FloatArray(arr.length()) { arr.getDouble(it).toFloat() }
        }
        scale = obj.getJSONArray("scale").let { arr ->
            FloatArray(arr.length()) { arr.getDouble(it).toFloat() }
        }
    }

    /**
     * Leser inn modellfila som en ByteBuffer, slik TensorFlow krever.
     */
    private fun loadModelFile(context: Context, fileName: String): ByteBuffer {
        val fileDescriptor = context.assets.openFd(fileName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    /**
     * Predikerer klasse (0–3) basert på høyest sannsynlighet i output.
     * Brukes der du vil vite hvilken kategori en fiskeplass havner i.
     */
    fun predict(input: FloatArray): Int {
        require(input.size == 10) { "Input must have 10 features" }

        val normalized = normalize(input)

        val inputBuffer = createInputBuffer(normalized)

        val output = Array(1) { FloatArray(4) } // 4 klasser
        interpreter.run(inputBuffer, output)

        val probs = output[0]
        val predictedClass = probs.indices.maxByOrNull { probs[it] } ?: -1

        Log.d("AI_PREDICTION", "Probabilities: ${probs.joinToString()}")
        Log.d("AI_PREDICTION", "Predicted class: $predictedClass (Confidence: ${probs[predictedClass]})")

        return predictedClass
    }

    /**
     * Returnerer sannsynlighetsfordeling over alle 4 klasser (float[4]).
     * Brukes for å beregne en score: f.eks. sannsynlighet for "bra" (klasse 2 + 3).
     */
    fun predictScores(input: FloatArray): FloatArray {
        require(input.size == 10) { "Input must have 10 features" }

        val normalized = normalize(input)

        val inputBuffer = createInputBuffer(normalized)

        val output = Array(1) { FloatArray(4) }
        interpreter.run(inputBuffer, output)

        Log.d("AI_PREDICTION", "Probabilities (score mode): ${output[0].joinToString()}")

        return output[0]
    }

    /**
     * Normaliserer input med z-score basert på mean og scale fra treningsdata.
     */
    private fun normalize(input: FloatArray): FloatArray {
        return FloatArray(input.size) { i ->
            (input[i] - mean[i]) / scale[i]
        }
    }

    /**
     * Oppretter input-buffer i riktig format (float32, native order).
     */
    private fun createInputBuffer(input: FloatArray): ByteBuffer {
        return ByteBuffer.allocateDirect(4 * input.size).apply {
            order(ByteOrder.nativeOrder())
            input.forEach { putFloat(it) }
            rewind()
        }
    }
}
