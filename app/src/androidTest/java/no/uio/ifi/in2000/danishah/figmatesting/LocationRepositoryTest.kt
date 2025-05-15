package no.uio.ifi.in2000.danishah.figmatesting

import no.uio.ifi.in2000.danishah.figmatesting.data.repository.LocationRepository
import no.uio.ifi.in2000.danishah.figmatesting.data.source.LocationDataSource
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationRepositoryTest {

    @Test
    fun resetSearchResultsClearsList() {
        // Lag en fake datasource med testvariabel
        val fakeDataSource = object : LocationDataSource() {
            var resetCalled = false

            override fun resetSessionToken() {
                resetCalled = true
            }
        }

        // Bruk fakeDataSource direkte
        val repo = LocationRepository(dataSource = fakeDataSource)

        // Kjør funksjonen vi tester
        repo.resetSearchResults()

        // Sjekk at listen er tom
        assertTrue(repo.searchResults.value.isEmpty())

        // Sjekk at resetSessionToken faktisk ble kalt
        assertTrue(fakeDataSource.resetCalled)
    }

}
