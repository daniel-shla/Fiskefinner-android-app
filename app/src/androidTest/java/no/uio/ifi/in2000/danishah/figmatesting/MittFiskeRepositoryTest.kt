package no.uio.ifi.in2000.danishah.figmatesting.data.repository

import kotlinx.coroutines.runBlocking
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.MittFiskeLocation
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.PointGeometry
import no.uio.ifi.in2000.danishah.figmatesting.data.source.MittFiskeDataSource
import org.junit.Assert.*
import org.junit.Test
import io.ktor.client.HttpClient

class MittFiskeRepositoryTest {

    private class FakeDataSource : MittFiskeDataSource(HttpClient()) {
        override suspend fun fetchLocations(
            polygonWKT: String,
            pointWKT: String,
            min: Int,
            max: Int
        ): List<MittFiskeLocation> {
            return listOf(
                MittFiskeLocation(
                    id = "1",
                    name = "Test Spot",
                    p = PointGeometry("Point", listOf(10.0, 60.0)),
                    locs = emptyList(),
                    rating = 5
                )
            )
        }
    }

    private class FailingDataSource : MittFiskeDataSource(HttpClient()) {
        override suspend fun fetchLocations(
            polygonWKT: String,
            pointWKT: String,
            min: Int,
            max: Int
        ): List<MittFiskeLocation> {
            throw RuntimeException("Fake failure")
        }
    }

    @Test
    fun getLocationsForAreaReturnsSuccess() = runBlocking {
        val repository = MittFiskeRepository(FakeDataSource())
        val result = repository.getLocationsForArea("polygon", "point", 0, 10)

        assertTrue(result.isSuccess)
        val locations = result.getOrNull()
        assertNotNull(locations)
        assertEquals(1, locations?.size)
        assertEquals("Test Spot", locations?.first()?.name)
    }

    @Test
    fun getLocationsForAreaReturnsFailure() = runBlocking {
        val repository = MittFiskeRepository(FailingDataSource())
        val result = repository.getLocationsForArea("polygon", "point", 0, 10)

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull(exception)
        assertEquals("Fake failure", exception?.message)
    }
}
