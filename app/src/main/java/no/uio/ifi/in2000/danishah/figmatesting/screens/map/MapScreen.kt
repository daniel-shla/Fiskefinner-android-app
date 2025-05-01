package no.uio.ifi.in2000.danishah.figmatesting.screens.map

import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.CoordinateBounds
import com.mapbox.maps.MapView
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.plugin.Plugin
import com.mapbox.maps.plugin.annotation.AnnotationPlugin
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import no.uio.ifi.in2000.danishah.figmatesting.R
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.MittFiskeLocation
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.toPoint
import no.uio.ifi.in2000.danishah.figmatesting.screens.map.cards.SearchResultsCard
import no.uio.ifi.in2000.danishah.figmatesting.screens.map.mittFiske.MittFiskeViewModel
import no.uio.ifi.in2000.danishah.figmatesting.screens.map.mittFiske.MittFiskeViewModelFactory
import no.uio.ifi.in2000.danishah.figmatesting.data.repository.MittFiskeRepository
import no.uio.ifi.in2000.danishah.figmatesting.data.source.MittFiskeDataSource
import io.ktor.client.HttpClient
import no.uio.ifi.in2000.danishah.figmatesting.data.source.LocationDataSource

@OptIn(FlowPreview::class)
@Composable
fun MapScreen(viewModel: MapViewModel = viewModel(factory = MapViewModel.Factory)) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSearchActive by viewModel.isSearchActive.collectAsState()
    val mapCenter by viewModel.mapCenter.collectAsState()
    val zoomLevel by viewModel.zoomLevel.collectAsState()
    val clusters by viewModel.clusters.collectAsState()

    val mapViewRef = remember { mutableStateOf<MapView?>(null) }
    var allFishingLocations by remember { mutableStateOf<List<MittFiskeLocation>>(emptyList()) }
    var locationsLoaded by remember { mutableStateOf(false) }

    val mapViewportState = rememberMapViewportState {
        setCameraOptions { center(mapCenter); zoom(zoomLevel) }
    }

    val mittFiskeViewModel: MittFiskeViewModel = viewModel(factory = MittFiskeViewModelFactory(MittFiskeRepository(MittFiskeDataSource(HttpClient()))))

    Box(modifier = Modifier.fillMaxSize()) {
        MapboxMap(
            modifier = Modifier.fillMaxSize(),
            mapViewportState = mapViewportState
        ) {
            LaunchedEffect(Unit) {
                if (!locationsLoaded) {
                    val polygonWKT = boundsToPolygonWKT(CoordinateBounds(Point.fromLngLat(4.0, 57.0), Point.fromLngLat(31.0, 71.0)))
                    mittFiskeViewModel.loadLocations(polygonWKT, "POINT(10 60)")
                    while (mittFiskeViewModel.uiState.value.isLoading) delay(100)
                    val locations = mittFiskeViewModel.uiState.value.locations
                    if (locations.isNotEmpty()) {
                        viewModel.rankAllFishingspots(locations)
                        allFishingLocations = locations
                        locationsLoaded = true
                    }
                }
            }

            MapEffect(mapViewportState) { mapView -> mapViewRef.value = mapView }

            LaunchedEffect(allFishingLocations) {
                if (allFishingLocations.isEmpty()) return@LaunchedEffect
                snapshotFlow { mapViewportState.cameraState }
                    .filterNotNull()
                    .map { it.center to it.zoom }
                    .distinctUntilChanged()
                    .debounce(300)
                    .collect { (center, zoom) ->
                        val mapView = mapViewRef.value ?: return@collect
                        val bounds = mapView.mapboxMap.coordinateBoundsForCamera(CameraOptions.Builder().center(center).zoom(zoom).build())
                        val visibleLocations = allFishingLocations.filter { bounds.contains(it.toPoint(), false) }
                        viewModel.updateClusters(visibleLocations, zoom)
                        viewModel.triggerDraw()

                        withContext(Dispatchers.Main) {
                            val manager = mapView.annotations.createPointAnnotationManager()
                            manager.deleteAll()
                            val bitmap = BitmapFactory.decodeResource(mapView.context.resources, R.drawable.yallaimg)
                            clusters.forEach { cluster ->
                                val options = PointAnnotationOptions()
                                    .withPoint(cluster.center)
                                    .withIconImage(bitmap)
                                    .withIconSize(if (cluster.spots.size == 1) 0.04 else 0.04)
                                    .withTextField(cluster.avgRating?.toString() ?: "-")
                                    .withTextOffset(listOf(0.0, -2.0))
                                    .withTextSize(12.0)
                                manager.create(options)
                            }
                        }
                    }
            }
        }

        // Search bar and controls here (similar to how you have it now)
    }
}

fun boundsToPolygonWKT(bounds: CoordinateBounds): String {
    val sw = bounds.southwest
    val ne = bounds.northeast
    return "POLYGON((" +
            "${sw.longitude()} ${ne.latitude()}," +
            "${sw.longitude()} ${sw.latitude()}, " +
            "${ne.longitude()} ${sw.latitude()}, " +
            "${ne.longitude()} ${ne.latitude()}, " +
            "${sw.longitude()} ${ne.latitude()}" +
            "))"
}