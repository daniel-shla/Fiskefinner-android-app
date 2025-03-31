package no.uio.ifi.in2000.danishah.figmatesting.data.source

import android.util.Log
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.FishingLocation
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.FishingLocationResponse


class SeaFishingLocationsDataSource {
    fun processApiResponse(response: FishingLocationResponse): List<FishingLocation> {
        return try {
            response.features.mapNotNull { feature ->
                val geometry = feature.geometry ?: return@mapNotNull null
                val rings = geometry.rings ?: return@mapNotNull null
                
                val fishTypesList = feature.attributes.fishTypes
                    ?.split(",")
                    ?.map { it.trim() }
                    ?.filter { it.isNotEmpty() }
                    ?: emptyList()

                // Convert geometry rings (as its in the QUERY) to list of coordinate pairs
                val polygonPoints = rings.map { ring ->
                    ring.map { point ->
                        // Point format is [longitude, latitude], SIDEBAR: skulle ønske alle ble enige om x,y eller y,x her :-(
                        // swap to Pair(latitude, longitude) for mapping
                        Pair(point[1], point[0])
                    }
                }

                FishingLocation(
                    name = feature.attributes.name,
                    fishTypes = fishTypesList,
                    polygonPoints = polygonPoints
                )
            }
        } catch (e: Exception) {
            Log.e("FishingLocationsDataSource", "Error processing API response", e)
            emptyList()
        }
    }
} 