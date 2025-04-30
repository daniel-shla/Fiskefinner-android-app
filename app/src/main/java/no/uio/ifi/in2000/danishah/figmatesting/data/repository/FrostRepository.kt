package no.uio.ifi.in2000.danishah.figmatesting.data.repository

import no.uio.ifi.in2000.danishah.figmatesting.data.source.FrostDataSource
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.FrostResponse

class FrostRepository(private val frostDataSource: FrostDataSource) {

    suspend fun getFrostData(): FrostResponse {
        return frostDataSource.fetchFrostData()
    }
}