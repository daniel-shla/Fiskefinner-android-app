package no.uio.ifi.in2000.danishah.figmatesting.screens.map

import no.uio.ifi.in2000.danishah.figmatesting.data.repository.LocationRepository
import no.uio.ifi.in2000.danishah.figmatesting.data.source.LocationDataSource
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock


class MapViewModelTest {

    private lateinit var viewModel: MapViewModel
    private val mockRepository: LocationRepository = mock()

    @Before
    fun setup() {
        viewModel = MapViewModel(mockRepository)
    }

    @Test
    fun `initial zoom level is set to country zoom`() {
        // This test simply checks that the initial zoom level matches the expected default
        assertEquals(LocationDataSource.COUNTRY_ZOOM, viewModel.zoomLevel.value, 0.0)
    }

    @Test
    fun `initial map center is set to Norway`() {
        // This test checks that the initial map center is Norway
        assertEquals(LocationDataSource.NORWAY_CENTER, viewModel.mapCenter.value)
    }

    @Test
    fun `zoom in correctly increases zoom level`() {
        // Get initial zoom
        val initialZoom = viewModel.zoomLevel.value
        
        // Call zoom in
        viewModel.zoomIn()
        
        // Check that zoom increased by 1
        assertEquals(initialZoom + 1.0, viewModel.zoomLevel.value, 0.0)
    }
} 