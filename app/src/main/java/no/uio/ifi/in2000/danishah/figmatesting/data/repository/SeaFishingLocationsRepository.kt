package no.uio.ifi.in2000.danishah.figmatesting.data.repository

import no.uio.ifi.in2000.danishah.figmatesting.data.apiclient.SeaFishingLocationsApiClient
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.FishingLocation
import no.uio.ifi.in2000.danishah.figmatesting.data.source.SeaFishingLocationsDataSource


class SeaFishingLocationsRepository(
    private val api: SeaFishingLocationsApiClient,
    private val dataSource: SeaFishingLocationsDataSource
) {
    suspend fun getFishingLocations(): List<FishingLocation> {
        return api.getFishingLocations()
    }

    fun filterLocationsByFishTypes(
        locations: List<FishingLocation>,
        fishTypes: List<String>
    ): List<FishingLocation> {
        if (fishTypes.isEmpty()) return locations

        return locations.filter { location ->
            fishTypes.any { fishType ->
                location.fishTypes.contains(fishType)
            }
        }
    }
} 