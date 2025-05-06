package no.uio.ifi.in2000.danishah.figmatesting.screens.map

import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.Cluster
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.FishSpeciesData
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.MittFiskeLocation
import no.uio.ifi.in2000.danishah.figmatesting.data.repository.MittFiskeRepository
import no.uio.ifi.in2000.danishah.figmatesting.data.source.LocationDataSource
import no.uio.ifi.in2000.danishah.figmatesting.data.source.MittFiskeDataSource
import no.uio.ifi.in2000.danishah.figmatesting.screens.dashboard.LoactionForecast.WeatherViewModel
import no.uio.ifi.in2000.danishah.figmatesting.screens.dashboard.PredictionViewModel
import no.uio.ifi.in2000.danishah.figmatesting.screens.fishselection.FishSpeciesViewModel
import no.uio.ifi.in2000.danishah.figmatesting.screens.map.cards.SearchResultsCard
import no.uio.ifi.in2000.danishah.figmatesting.screens.map.components.getColorForSpecies
import no.uio.ifi.in2000.danishah.figmatesting.screens.map.mittFiske.MittFiskeViewModel
import no.uio.ifi.in2000.danishah.figmatesting.screens.map.mittFiske.MittFiskeViewModelFactory
import android.graphics.Color as AndroidColor

@OptIn(FlowPreview::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = viewModel(factory = MapViewModel.Factory),
    fishSpeciesViewModel: FishSpeciesViewModel
) {
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
    
    // Using the passed FishSpeciesViewModel instead of creating a new one
    val availableSpecies by fishSpeciesViewModel.availableSpecies.collectAsState()
    val speciesStates by fishSpeciesViewModel.speciesStates.collectAsState()
    val isLoadingSpecies by fishSpeciesViewModel.isLoading.collectAsState()
    val showSpeciesPanel by fishSpeciesViewModel.showSpeciesPanel.collectAsState()

    val weatherViewModel: WeatherViewModel = viewModel()
    val predictionViewModel: PredictionViewModel = viewModel()

    val annotationToLocation = remember { mutableMapOf<String, MittFiskeLocation>() }
    val selectedLocation = remember { mutableStateOf<MittFiskeLocation?>(null) }
    val selectedCluster = remember { mutableStateOf<Cluster?>(null) }





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

    LaunchedEffect(Unit) {
        if (mittFiskeState.locations.isEmpty()) {
            val polygonWKT = "POLYGON((4.0 71.5, 4.0 57.9, 31.5 57.9, 31.5 71.5, 4.0 71.5))"
            val pointWKT = "POINT(15.0 64.0)"
            mittFiskeViewModel.loadLocations(polygonWKT, pointWKT)
        }

        mittFiskeViewModel.rateAllLocationsWithAI(
            weatherViewModel = weatherViewModel,
            predictionViewModel = predictionViewModel
        )
    }

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
        
        if (enabledSpecies.isEmpty()) {
            return@LaunchedEffect
        }
        
        // Clear existing polygons
        polygonAnnotationManagerRef.value?.deleteAll()
        
        // Check if polygonAnnotationManagerRef is initialized
        if (polygonAnnotationManagerRef.value == null) {
            return@LaunchedEffect
        }
        
        // Draw each enabled species
        enabledSpecies.forEach { state ->
            if (state.species.polygons.isNotEmpty()) {
                polygonAnnotationManagerRef.value?.let { manager ->
                    drawFishSpeciesPolygons(
                        manager, 
                        state.species, 
                        getColorForSpecies(state.species.scientificName),
                        state.opacity
                    )
                }
            }
        }
    }
    
    // Additional effect to ensure polygons are drawn when map is focused after navigation
    LaunchedEffect(Unit) {
        // Short delay to ensure map is fully loaded
        delay(500)
        
        val enabledSpecies = speciesStates.values.filter { it.isEnabled && it.isLoaded }
        if (enabledSpecies.isNotEmpty() && polygonAnnotationManagerRef.value != null) {
            // Force clear and redraw
            polygonAnnotationManagerRef.value?.deleteAll()
            
            enabledSpecies.forEach { state ->
                if (state.species.polygons.isNotEmpty()) {
                    polygonAnnotationManagerRef.value?.let { manager ->
                        drawFishSpeciesPolygons(
                            manager, 
                            state.species, 
                            getColorForSpecies(state.species.scientificName),
                            state.opacity
                        )
                    }
                }
            }
            
            // Remove automatic navigation to polygon location
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


                            val locations = mittFiskeViewModel.uiState.value.locations

                            val visibleLocations = mittFiskeViewModel.filterLocationsInBounds(locations, bounds)


                            if (locations.isNotEmpty()) {
                                viewModel.updateClusters(visibleLocations, zoom)
                                viewModel.triggerDraw()

                                withContext(Dispatchers.Main) { // Bare UI-arbeid

                                    val manager = annotationManagerRef.value ?: return@withContext
                                    manager.deleteAll()

                                    val bitmap = BitmapFactory.decodeResource(mapView.context.resources, R.drawable.yallaimg)

                                    viewModel.clusters.value.forEach { cluster ->
                                        val options = PointAnnotationOptions()
                                            .withPoint(cluster.center)
                                            .withIconImage(bitmap)
                                            .withIconSize(if (cluster.spots.size == 1) 0.04 else 0.040)
                                            .withTextField(cluster.averageRating.toString())
                                            .withTextOffset(listOf(0.0, -2.0))
                                            .withTextSize(12.0)
                                        val annotation = manager.create(options)
                                        annotationToLocation[annotation.id] = cluster.spots.first() // velger første lokasjon i cluster
                                    }

                                    manager.addClickListener { annotation ->
                                        val location = annotationToLocation[annotation.id]
                                        if (location != null) {
                                            val cluster = viewModel.clusters.value.find { it.spots.contains(location) }
                                            if (cluster != null) {
                                                if (cluster.spots.size == 1) {
                                                    selectedLocation.value = location
                                                    selectedCluster.value = null
                                                } else {
                                                    selectedLocation.value = null
                                                    selectedCluster.value = cluster
                                                }
                                            }
                                        }
                                        true
                                    }
                                }
                            }
                        }
                    }
            }
        }

        selectedCluster.value?.let { cluster ->
            Box(modifier = Modifier.fillMaxSize()) {
                ClusterOverviewCard(
                    cluster = selectedCluster.value!!,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 90.dp)
                )
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
            selectedLocation.value?.let { location ->
                LocationInfoCard(
                    location = location,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                )
            }

// Vis info hvis en cluster med flere spots er valgt

        }

        // Zoom controls and My Location - at bottom right
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Remove Fish Species button - now in FishSelectionScreen
            
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
        
        // Show loading indicator for fish data
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
    }
}

// Function to draw fish species polygons
private fun drawFishSpeciesPolygons(
    polygonManager: PolygonAnnotationManager?, 
    fishSpecies: FishSpeciesData,
    color: Color,
    opacity: Float
) {
    // Safety check for null manager
    if (polygonManager == null || fishSpecies.polygons.isEmpty()) {
        return
    }
    
    // Increased max polygons to take advantage of simplified files
    val maxPolygons = 2000 // Increased from 1000
    val polygonsToShow = if (fishSpecies.polygons.size > maxPolygons) {
        val step = fishSpecies.polygons.size / maxPolygons
        fishSpecies.polygons.filterIndexed { index, _ -> index % step == 0 }.take(maxPolygons)
    } else {
        fishSpecies.polygons
    }
    
    // Convert the Jetpack Compose color to Android color with alpha
    val colorWithoutAlpha = "#${color.toArgb().toHexString().substring(2)}"
    
    // Create colors with the proper opacity/alpha values
    val fillColor = AndroidColor.parseColor(colorWithoutAlpha)
    val fillOpacity = opacity.toDouble()
    val outlineColor = AndroidColor.BLACK
    
    var firstPolygonFirstPoint: Point? = null
    
    // Batch processing polygons to improve performance
    val batchSize = 100
    val batches = polygonsToShow.chunked(batchSize)
    
    batches.forEach { batchPolygons ->
        val batchAnnotations = batchPolygons.mapNotNull { polygon ->
            if (polygon.size < 3) return@mapNotNull null
            
            val points = polygon.map { (lat, lng) -> Point.fromLngLat(lng, lat) }
            val closedPoints = if (points.first() != points.last()) points + points.first() else points
            
            if (firstPolygonFirstPoint == null) {
                firstPolygonFirstPoint = closedPoints.firstOrNull()
            }
            
            PolygonAnnotationOptions()
                .withPoints(listOf(closedPoints))
                .withFillColor(fillColor)
                .withFillOpacity(fillOpacity)
                .withFillOutlineColor(outlineColor)
        }
        
        // Create polygon annotations in batch
        polygonManager.create(batchAnnotations)
    }
    
    // Store the first polygon's first point for teleportation
    if (firstPolygonFirstPoint != null) {
        lastFirstPolygonPoint = firstPolygonFirstPoint
    }
}

// Extension function to convert Int color to hex string
private fun Int.toHexString(): String {
    return String.format("%08X", this)
}

// Store the last first polygon point for teleportation
private var lastFirstPolygonPoint: Point? = null

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

@Composable
fun LocationInfoCard(location: MittFiskeLocation, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Fiskeplass: ${location.name}", style = MaterialTheme.typography.titleMedium)
            Text("Rating: ${location.rating ?: "ukjent"}")
            Spacer(Modifier.height(8.dp))
            Text("Antall registrerte posisjoner: ${location.locs.size}")
            // TODO: Vis evt arter hvis du har
        }
    }
}

@Composable
fun ClusterOverviewCard(cluster: Cluster, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth(0.92f)
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = "${cluster.spots.size} fiskeplasser i dette området",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            cluster.spots.take(3).forEach { spot ->
                val loc = spot.locs.firstOrNull()
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    Text(
                        text = spot.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    loc?.de?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⭐ ${spot.rating ?: "?"}")
                        loc?.fe?.takeIf { it.isNotEmpty() }?.let { features ->
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = features.joinToString(" • "),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (cluster.spots.size > 3) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "…og ${cluster.spots.size - 3} til",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            cluster.averageRating?.let {
                Text(
                    text = "Gjennomsnittlig AI-vurdering: ${"%.1f".format(it)} / 5",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}


