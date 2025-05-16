package no.uio.ifi.in2000.danishah.figmatesting

import io.ktor.client.HttpClient
import kotlinx.coroutines.test.runTest
import no.uio.ifi.in2000.danishah.figmatesting.data.repository.MittFiskeRepository
import no.uio.ifi.in2000.danishah.figmatesting.data.source.MittFiskeDataSource
import no.uio.ifi.in2000.danishah.figmatesting.screens.map.mittFiske.MittFiskeViewModel
import org.junit.Assert.assertEquals
import org.junit.Test

class MittFiskeViewModelTest {

    @Test
    fun selectSpeciesUpdatesUiState() = runTest {
        val fakeDataSource = object : MittFiskeDataSource(HttpClient()) {}
        val repo = MittFiskeRepository(fakeDataSource)
        val viewModel = MittFiskeViewModel(repo)

        viewModel.selectSpecies("ørret")

        assertEquals("ørret", viewModel.uiState.value.selectedSpecies)
    }
}
