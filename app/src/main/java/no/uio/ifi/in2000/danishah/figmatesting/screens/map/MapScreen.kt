package no.uio.ifi.in2000.danishah.figmatesting.screens.map

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
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
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPolygonAnnotationManager
import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import no.uio.ifi.in2000.danishah.figmatesting.R
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.FishSpeciesData
import no.uio.ifi.in2000.danishah.figmatesting.data.repository.MittFiskeRepository
import no.uio.ifi.in2000.danishah.figmatesting.data.source.LocationDataSource
import no.uio.ifi.in2000.danishah.figmatesting.data.source.MittFiskeDataSource
import no.uio.ifi.in2000.danishah.figmatesting.screens.map.cards.SearchResultsCard
import no.uio.ifi.in2000.danishah.figmatesting.screens.map.components.SpeciesPanel
import no.uio.ifi.in2000.danishah.figmatesting.screens.map.components.getColorForSpecies
import no.uio.ifi.in2000.danishah.figmatesting.screens.map.mittFiske.MittFiskeViewModel
import no.uio.ifi.in2000.danishah.figmatesting.screens.map.mittFiske.MittFiskeViewModelFactory
import android.graphics.Color as AndroidColor


@OptIn(FlowPreview::class)
@Composable
fun MapScreen(viewModel: MapViewModel = viewModel(factory = MapViewModel.Factory)) {
    // Get screen density for map initialization
    val density = LocalDensity.current
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    // Collect state from ViewModel
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSearchActive by viewModel.isSearchActive.collectAsState()
    val mapCenter by viewModel.mapCenter.collectAsState()
    val zoomLevel by viewModel.zoomLevel.collectAsState()
    val showMinCharsHint by viewModel.showMinCharsHint.collectAsState()
    val clusters by viewModel.clusters.collectAsState()

    val mapViewRef = remember { mutableStateOf<MapView?>(null) }
    val currentBounds = remember { mutableStateOf<CoordinateBounds?>(null) }
    val annotationManagerRef = remember { mutableStateOf<PointAnnotationManager?>(null) }
    // New state for polygon annotation manager
    val polygonAnnotationManagerRef = remember { mutableStateOf<PolygonAnnotationManager?>(null) }
    
    // Create FishSpeciesViewModel
    val fishSpeciesViewModel: FishSpeciesViewModel = viewModel(factory = FishSpeciesViewModel.Factory(context.applicationContext as android.app.Application))
    val availableSpecies by fishSpeciesViewModel.availableSpecies.collectAsState()
    val speciesStates by fishSpeciesViewModel.speciesStates.collectAsState()
    val isLoadingSpecies by fishSpeciesViewModel.isLoading.collectAsState()
    val showSpeciesPanel by fishSpeciesViewModel.showSpeciesPanel.collectAsState()

    // Using the recommended viewport state approach
    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            center(mapCenter)
            zoom(zoomLevel)
            pitch(0.0)
            bearing(0.0)
        }
    }

    val mittFiskeViewModel: MittFiskeViewModel = viewModel(
        factory = MittFiskeViewModelFactory(
            MittFiskeRepository(
                MittFiskeDataSource(HttpClient())
            )
        )
    )
    val mittFiskeState by mittFiskeViewModel.uiState.collectAsState()

    LaunchedEffect(mapCenter, zoomLevel) {
        val cameraOptions = CameraOptions.Builder()
            .center(mapCenter)
            .zoom(zoomLevel)
            .pitch(0.0)
            .bearing(0.0)
            .build()

        mapViewportState.setCameraOptions(cameraOptions)
    }

    // BEHOLD NULLCHECKS VED CAMERASTATE; IKKE ENDRE!
    remember(mapViewportState.cameraState?.center, mapViewportState.cameraState?.zoom) {
        mapViewportState.cameraState?.center?.let { center ->
            mapViewportState.cameraState!!.zoom.let { zoom ->
                viewModel.updateMapPosition(center, zoom)
            }
        }
        true
    }

    // Effect to draw fish species polygons when species states change
    LaunchedEffect(speciesStates) {
        // Get all enabled species
        val enabledSpecies = speciesStates.values
            .filter { it.isEnabled && it.isLoaded }
            .map { it.species }
        
        Log.d("FishPolygons", "Enabled species count: ${enabledSpecies.size}")
        
        // Clear existing polygons
        polygonAnnotationManagerRef.value?.deleteAll()
        
        // Draw each enabled species
        enabledSpecies.forEach { species ->
            if (species.polygons.isNotEmpty()) {
                Log.d("FishPolygons", "Drawing ${species.polygons.size} polygons for ${species.commonName}")
                polygonAnnotationManagerRef.value?.let { manager ->
                    drawFishSpeciesPolygons(manager, species, getColorForSpecies(species.scientificName))
                }
            } else {
                Log.e("FishPolygons", "No polygons available for ${species.scientificName}")
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MapboxMap(
            modifier = Modifier.fillMaxSize(),
            mapViewportState = mapViewportState,
        ){
            val drawPoints by viewModel.shouldDraw.collectAsState()

            LaunchedEffect(mittFiskeState.isLoading) {
                while (mittFiskeState.isLoading) {
                    delay(100)
                }
                if (mittFiskeState.locations.isNotEmpty()) {
                    viewModel.triggerDraw()
                }
            }

            MapEffect(mapViewportState) { mapView ->
                mapViewRef.value = mapView

                val plugin = mapView.getPlugin(Plugin.MAPBOX_ANNOTATION_PLUGIN_ID) as? AnnotationPlugin
                if (annotationManagerRef.value == null) {
                    annotationManagerRef.value = plugin?.createPointAnnotationManager()
                }
                
                // Create polygon annotation manager if not already created
                if (polygonAnnotationManagerRef.value == null) {
                    polygonAnnotationManagerRef.value = plugin?.createPolygonAnnotationManager()
                }

                val bounds = mapView.getMapboxMap().coordinateBoundsForCamera(
                    CameraOptions.Builder()
                        .center(mapViewportState.cameraState?.center)
                        .zoom(mapViewportState.cameraState?.zoom)
                        .build()
                )
                currentBounds.value = bounds
            }


            LaunchedEffect(Unit) {
                snapshotFlow { mapViewportState.cameraState }
                    .filterNotNull()
                    .map { cameraState -> cameraState.center to cameraState.zoom }
                    .distinctUntilChanged()
                    .debounce(300)//kan justeres
                    .collect { (center, zoom) ->
                        withContext(Dispatchers.Default) { //background work

                            val mapView = mapViewRef.value ?: return@withContext
                            val bounds = mapView.mapboxMap.coordinateBoundsForCamera(
                                CameraOptions.Builder().center(center).zoom(zoom).build()
                            )

                            val polygonWKT = boundsToPolygonWKT(bounds)
                            val pointWKT = "POINT(${center.longitude()} ${center.latitude()})"

                            mittFiskeViewModel.loadLocations(polygonWKT, pointWKT)

                            while (mittFiskeViewModel.uiState.value.isLoading) {
                                delay(100)
                            }

                            val locations = mittFiskeViewModel.uiState.value.locations

                            if (locations.isNotEmpty()) {
                                viewModel.updateClusters(locations, zoom)
                                viewModel.triggerDraw()

                                withContext(Dispatchers.Main) { // Bare UI-arbeid
                                    val manager = annotationManagerRef.value ?: return@withContext
                                    manager.deleteAll()
                                    Log.d("Annotation", "Sletter alle")

                                    val bitmap = BitmapFactory.decodeResource(mapView.context.resources, R.drawable.yallaimg)

                                    viewModel.clusters.value.forEach { cluster ->
                                        val options = PointAnnotationOptions()
                                            .withPoint(cluster.center)
                                            .withIconImage(bitmap)
                                            .withIconSize(if (cluster.spots.size == 1) 0.04 else 0.040)
                                            .withTextField(cluster.spots.size.toString())
                                            .withTextOffset(listOf(0.0, -2.0))
                                            .withTextSize(12.0)
                                        manager.create(options)
                                    }
                                }
                            }
                        }
                    }
            }
        }

        // Search bar container
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 30.dp)
                .fillMaxWidth(0.9f)
        ) {
            // CUSTOM search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp), // Litt høyere enn 48dp, men fortsatt kompakt
                placeholder = {
                    Text(
                        "Søk etter norske byer...",
                        style = MaterialTheme.typography.bodySmall // mindre font for å passe inn
                    )
                },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = {
                        if (searchQuery.isNotEmpty()) {
                            focusManager.clearFocus()
                            viewModel.searchAndNavigate(searchQuery)
                            viewModel.setSearchActive(false)
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }
                },
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                ),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (searchQuery.isNotEmpty()) {
                            focusManager.clearFocus()
                            viewModel.searchAndNavigate(searchQuery)
                            viewModel.setSearchActive(false)
                        }
                    }
                )
            )


            // Show search results as a dropdown
            if (isSearchActive) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 56.dp) // below the search bar
                ) {
                    SearchResultsCard(
                        isLoading = isLoading,
                        searchResults = searchResults,
                        searchQuery = searchQuery,
                        showMinCharsHint = showMinCharsHint,
                        onSuggestionClick = { suggestion ->
                            viewModel.selectSuggestion(suggestion)
                            viewModel.navigateToLocation(suggestion) // naviger kartet
                            viewModel.setSearchActive(false)         // skjul resultatlista
                            focusManager.clearFocus()                // skjul tastatur
                        }

                    )
                }
            }
        }

        // Zoom controls and My Location - at bottom right
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Draw Fish Species button
            SmallFloatingActionButton(
                onClick = {
                    fishSpeciesViewModel.toggleSpeciesPanel(true)
                },
                shape = CircleShape,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "Velg fiskearter"
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Zoom in button
            SmallFloatingActionButton(
                onClick = {
                    viewModel.zoomIn()
                },
                shape = CircleShape,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zoom inn")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Zoom out button
            SmallFloatingActionButton(
                onClick = {
                    viewModel.zoomOut()
                },
                shape = CircleShape,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom ut")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // My Location button
            SmallFloatingActionButton(
                onClick = {
                    viewModel.navigateToPoint(LocationDataSource.OSLO_LOCATION)
                },
                shape = CircleShape,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Min posisjon")
            }
        }

        // Show info card for selected fish species
        val enabledSpecies = speciesStates.values.filter { it.isEnabled && it.isLoaded }
        if (enabledSpecies.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth(0.9f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    enabledSpecies.forEach { state ->
                        Text(
                            text = state.species.commonName,
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        Text(
                            text = "Vitenskapelig navn: ${state.species.scientificName.replace("_", " ")}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    // Close button
                    SmallFloatingActionButton(
                        onClick = { 
                            enabledSpecies.forEach { state ->
                                fishSpeciesViewModel.toggleSpecies(state.species.scientificName)
                            }
                            polygonAnnotationManagerRef.value?.deleteAll()
                        },
                        shape = CircleShape,
                        modifier = Modifier
                            .align(Alignment.End)
                            .size(30.dp)
                    ) {
                        Text("X")
                    }
                }
            }
        }
        
        // Loading when fish species data is being loaded
        if (isLoadingSpecies) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 90.dp)
            ) {
                Card(
                    modifier = Modifier.padding(8.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Laster fiskedata...",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
        
        // Show fish species panel
        if (showSpeciesPanel) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
            ) {
                SpeciesPanel(
                    viewModel = fishSpeciesViewModel,
                    onClose = {
                        fishSpeciesViewModel.toggleSpeciesPanel(false)
                    }
                )
            }
        }

        // Teleport to First Polygon button
        if (lastFirstPolygonPoint != null) {
            SmallFloatingActionButton(
                onClick = {
                    // Move map center to the first polygon's first point
                    viewModel.navigateToPoint(lastFirstPolygonPoint!!)
                },
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .size(40.dp)
            ) {
                Text("Go")
            }
        }
    }
}

// Function to draw fish species polygons
private fun drawFishSpeciesPolygons(
    polygonManager: PolygonAnnotationManager?, 
    fishSpecies: FishSpeciesData,
    color: Color
) {
    Log.d("FishPolygons", "Starting to draw polygons for ${fishSpecies.scientificName}")
    
    val maxPolygons = 1000
    val polygonsToShow = if (fishSpecies.polygons.size > maxPolygons) {
        Log.d("FishPolygons", "Limiting from ${fishSpecies.polygons.size} to $maxPolygons polygons")
        val step = fishSpecies.polygons.size / maxPolygons
        fishSpecies.polygons.filterIndexed { index, _ -> index % step == 0 }.take(maxPolygons)
    } else {
        fishSpecies.polygons
    }
    
    val fillColor = "#CCFF0000" // Bright red, 80% opacity
    val outlineColor = "#FF000000" // Solid black
    var firstPolygonFirstPoint: Point? = null
    
    polygonsToShow.forEachIndexed { idx, polygon ->
        if (polygon.size < 3) return@forEachIndexed
        val points = polygon.map { (lat, lng) -> Point.fromLngLat(lng, lat) }
        val closedPoints = if (points.first() != points.last()) points + points.first() else points
        if (idx == 0) firstPolygonFirstPoint = closedPoints.firstOrNull()
        val polygonOptions = PolygonAnnotationOptions()
            .withPoints(listOf(closedPoints))
            .withFillColor(AndroidColor.parseColor(fillColor))
            .withFillOpacity(0.8)
            .withFillOutlineColor(AndroidColor.parseColor(outlineColor))
        polygonManager?.create(polygonOptions)
    }
    // Store the first polygon's first point for teleportation
    if (firstPolygonFirstPoint != null) {
        lastFirstPolygonPoint = firstPolygonFirstPoint
    }
}

// Store the last first polygon point for teleportation
private var lastFirstPolygonPoint: Point? = null

// Helper function to check if two points are equal (within a small epsilon)
private fun arePointsEqual(p1: Point, p2: Point): Boolean {
    val epsilon = 1e-8 // Small value to account for floating point imprecision
    return Math.abs(p1.longitude() - p2.longitude()) < epsilon && 
           Math.abs(p1.latitude() - p2.latitude()) < epsilon
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