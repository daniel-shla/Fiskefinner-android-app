package no.uio.ifi.in2000.danishah.figmatesting.data.repository

import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.MittFiskeLocation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.uio.ifi.in2000.danishah.figmatesting.data.source.MittFiskeDataSource

class MittFiskeRepository(
    private val dataSource: MittFiskeDataSource
) {
    suspend fun getLocationsForArea(
        polygonWKT: String,
        pointWKT: String,
        min: Int,
        max: Int
    ): Result<List<MittFiskeLocation>> = withContext(Dispatchers.IO) {
        try {
            val locations = dataSource.fetchLocations(polygonWKT, pointWKT, min, max)
            Result.success(locations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
