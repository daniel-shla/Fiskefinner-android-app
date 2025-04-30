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

/**
 * Repository for working with fish species data from GeoJSON files in assets
 */
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
    }
    
    /**
     * Get a list of all available fish species with their scientific and common names
     */
    fun getAvailableFishSpecies(): List<FishSpeciesData> {
        return AVAILABLE_SPECIES.map { scientificName ->
            FishSpeciesData(
                scientificName = scientificName,
                commonName = FishSpeciesData.getCommonName(scientificName),
                polygons = emptyList() // Polygons are loaded separately for performance
            )
        }
    }
    
    /**
     * Load polygon data for a specific fish species from GeoJSON file
     */
    suspend fun loadFishSpeciesPolygons(scientificName: String): FishSpeciesData? {
        return withContext(Dispatchers.IO) {
            try {
                // Format the file name based on scientific name
                val jsonFileName = "${scientificName.replace("_", " ")}.json"
                val fixedFileName = if (!assetExists(jsonFileName)) {
                    // Try alternative formats if the default one doesn't exist
                    val alternativeNames = listOf(
                        "${scientificName}.json",                              // dicentrarchus_labrax.json
                        "${scientificName.split("_").last()}.json",            // labrax.json
                        "${scientificName.split("_").first()}.json"            // dicentrarchus.json
                    )
                    
                    alternativeNames.find { assetExists(it) } ?: jsonFileName
                } else {
                    jsonFileName
                }
                
                Log.d(TAG, "Loading fish species data from $fixedFileName")
                
                if (!assetExists(fixedFileName)) {
                    Log.e(TAG, "File $fixedFileName does not exist in assets")
                    return@withContext null
                }
                
                // Parse the GeoJSON file with streaming to avoid memory issues
                context.assets.open(fixedFileName).use { inputStream ->
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val geoJsonData = gson.fromJson(reader, GeoJsonFishData::class.java)
                    
                    // Check if we successfully parsed the data
                    if (geoJsonData.features.isNotEmpty()) {
                        Log.d(TAG, "Successfully parsed GeoJSON with ${geoJsonData.features.size} features")
                        
                        // Convert to our internal format
                        val fishSpeciesData = geoJsonData.toFishSpeciesData(scientificName)
                        
                        Log.d(TAG, "Converted GeoJSON to ${fishSpeciesData.polygons.size} polygons for ${fishSpeciesData.scientificName}")
                        
                        return@withContext fishSpeciesData
                    } else {
                        Log.e(TAG, "No features found in GeoJSON file $fixedFileName")
                        null
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading fish species data for $scientificName", e)
                null
            }
        }
    }
    
    /**
     * Check if an asset file exists
     */
    private fun assetExists(fileName: String): Boolean {
        return try {
            context.assets.open(fileName).use { true }
        } catch (e: Exception) {
            false
        }
    }
} 