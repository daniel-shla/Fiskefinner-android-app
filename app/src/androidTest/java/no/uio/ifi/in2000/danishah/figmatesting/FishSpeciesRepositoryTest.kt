package no.uio.ifi.in2000.danishah.figmatesting

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import no.uio.ifi.in2000.danishah.figmatesting.data.repository.FishSpeciesRepository
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FishSpeciesRepositoryInstrumentedTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val repository = FishSpeciesRepository(context)

    @Test
    fun getAvailableFishSpeciesReturnsExpectedSpecies() {
        val species = repository.getAvailableFishSpecies()

        assertEquals(12, species.size)
        assertTrue(species.any { it.scientificName == "gadus_morhua" })

        val torsk = species.find { it.scientificName == "gadus_morhua" }
        assertNotNull(torsk)
        assertEquals("Torsk", torsk?.commonName)


        species.forEach {
            assertTrue("Polygons should be empty by default", it.polygons.isEmpty())
        }
    }
}
