package no.uio.ifi.in2000.danishah.figmatesting

import no.uio.ifi.in2000.danishah.figmatesting.data.repository.LocationRepository
import no.uio.ifi.in2000.danishah.figmatesting.data.source.LocationDataSource
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationRepositoryTest {

    @Test
    fun resetSearchResultsClearsList() {
        val fakeDataSource = object : LocationDataSource() {
            var resetCalled = false

            override fun resetSessionToken() {
                resetCalled = true
            }
        }

        val repo = LocationRepository(dataSource = fakeDataSource)

        repo.resetSearchResults()

        assertTrue(repo.searchResults.value.isEmpty())

        assertTrue(fakeDataSource.resetCalled)
    }

}
