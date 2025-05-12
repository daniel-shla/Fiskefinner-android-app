package no.uio.ifi.in2000.danishah.figmatesting.data.repository

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.FishSpeciesData
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.GeoJsonFishData
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.toFishSpeciesData
import java.io.BufferedReader
import java.io.InputStreamReader


class FishSpeciesRepository(private val context: Context) {
    
    // Create a Gson instance that is more tolerant of malformed JSON
    private val gson = GsonBuilder().setLenient().create()
    private val TAG = "FishSpeciesRepository"
    
    companion object {
        // List of all available fish species in assets - now based on GeoJSON files
        private val AVAILABLE_SPECIES = listOf(
            "dicentrarchus_labrax",
            "gadus_morhua",
            "melanogrammus_aeglefinus",
            "pollachius_virens",
            "scomber_scombrus",
            "pleuronectes_platessa",
            "hippoglossus_hippoglossus",
            "anarhichas_lupus",
            "esox_lucius",
            "salmo_salar",
            "salvelinus_alpinus",
            "perca_fluviatilis"
        )
        
        // Map of species to custom sampling rates for very large files
        private val SPECIES_SAMPLING_RATES = mapOf(
            "pollachius_virens" to 20 // Sample every 20th polygon for sei
        )
    }
    

    fun getAvailableFishSpecies(): List<FishSpeciesData> {
        return AVAILABLE_SPECIES.map { scientificName ->
            FishSpeciesData(
                scientificName = scientificName,
                commonName = FishSpeciesData.getCommonName(scientificName),
                polygons = emptyList() // Polygons are loaded separately for performance
            )
        }
    }
    

    suspend fun loadFishSpeciesPolygons(scientificName: String): FishSpeciesData? {
        return withContext(Dispatchers.IO) {
            try {
                val simplifiedFileName = "simplified_${scientificName}.json"
                val originalFileName = "${scientificName.replace("_", " ")}.json"
                
                val fileNameToUse = when {
                    assetExists(simplifiedFileName) -> {
                        Log.d(TAG, "Using simplified data file: $simplifiedFileName")
                        simplifiedFileName
                    }
                    assetExists(originalFileName) -> originalFileName
                    else -> {
                        val alternativeNames = listOf(
                            "${scientificName}.json",                         // dicentrarchus_labrax.json
                            "${scientificName.split("_").last()}.json",       // labrax.json
                            "${scientificName.split("_").first()}.json"       // dicentrarchus.json
                        )
                        
                        alternativeNames.find { assetExists(it) } ?: originalFileName
                    }
                }
                
                Log.d(TAG, "Loading fish species data from $fileNameToUse")
                
                if (!assetExists(fileNameToUse)) {
                    Log.e(TAG, "File $fileNameToUse does not exist in assets")
                    return@withContext null
                }
                
                // Get file size to determine if we need special handling
                val fileSize = getAssetFileSize(fileNameToUse)
                Log.d(TAG, "File size of $fileNameToUse: ${fileSize / (1024 * 1024)} MB")
                
                // Parse the GeoJSON file with streaming to avoid memory issues
                context.assets.open(fileNameToUse).use { inputStream ->
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val geoJsonData = gson.fromJson(reader, GeoJsonFishData::class.java)
                    
                    // Check if we successfully parsed the data
                    if (geoJsonData.features.isNotEmpty()) {
                        Log.d(TAG, "Successfully parsed GeoJSON with ${geoJsonData.features.size} features")
                        
                        // Convert to our internal format
                        val fishSpeciesData = geoJsonData.toFishSpeciesData(scientificName)
                        
                        // Apply additional sampling for very large files
                        val samplingRate = SPECIES_SAMPLING_RATES[scientificName] ?: 1
                        val sampledPolygons = if (samplingRate > 1) {
                            Log.d(TAG, "Applying sampling rate of 1:$samplingRate for $scientificName")
                            fishSpeciesData.polygons.filterIndexed { index, _ -> index % samplingRate == 0 }
                        } else {
                            fishSpeciesData.polygons
                        }
                        
                        val result = fishSpeciesData.copy(polygons = sampledPolygons)
                        Log.d(TAG, "Converted GeoJSON to ${result.polygons.size} polygons for ${result.scientificName}")
                        
                        return@withContext result
                    } else {
                        Log.e(TAG, "No features found in GeoJSON file $fileNameToUse")
                        null
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading fish species data for $scientificName", e)
                null
            }
        }
    }
    

    private fun assetExists(fileName: String): Boolean {
        return try {
            context.assets.open(fileName).use { true }
        } catch (e: Exception) {
            false
        }
    }
    

    private fun getAssetFileSize(fileName: String): Long {
        return try {
            context.assets.openFd(fileName).length
        } catch (e: Exception) {
            Log.e(TAG, "Could not determine file size for $fileName", e)
            -1
        }
    }
} 