package no.uio.ifi.in2000.danishah.figmatesting.screens.map

import android.graphics.BitmapFactory
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
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
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
import com.mapbox.maps.plugin.annotation.generated.OnPolygonAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPolygonAnnotationManager
import io.ktor.client.HttpClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.danishah.figmatesting.R
import no.uio.ifi.in2000.danishah.figmatesting.data.apiclient.SeaFishingLocationsApiClient
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.FishingLocation
import no.uio.ifi.in2000.danishah.figmatesting.data.repository.MittFiskeRepository
import no.uio.ifi.in2000.danishah.figmatesting.data.repository.SeaFishingLocationsRepository
import no.uio.ifi.in2000.danishah.figmatesting.data.source.LocationDataSource
import no.uio.ifi.in2000.danishah.figmatesting.data.source.MittFiskeDataSource
import no.uio.ifi.in2000.danishah.figmatesting.data.source.SeaFishingLocationsDataSource
import no.uio.ifi.in2000.danishah.figmatesting.screens.map.cards.SearchResultsCard
import no.uio.ifi.in2000.danishah.figmatesting.screens.map.mittFiske.MittFiskeViewModel
import no.uio.ifi.in2000.danishah.figmatesting.screens.map.mittFiske.MittFiskeViewModelFactory
import android.graphics.Color as AndroidColor


@Composable
fun MapScreen(viewModel: MapViewModel = viewModel(factory = MapViewModel.Factory)) {
    // Get screen density for map initialization
    val density = LocalDensity.current
    val focusManager = LocalFocusManager.current

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
    
    // State for fishing locations
    var fishingLocations by remember { mutableStateOf<List<FishingLocation>>(emptyList()) }
    // State to track if fishing locations are being loaded
    var isLoadingFishingLocations by remember { mutableStateOf(false) }
    // State to track the currently selected fishing location
    var selectedFishingLocation by remember { mutableStateOf<FishingLocation?>(null) }
    // State to track if fishing locations should be visible
    var showFishingLocations by remember { mutableStateOf(false) }
    // State to track if locations have been loaded already
    var locationsLoaded by remember { mutableStateOf(false) }




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
    
    // Create an instance of SeaFishingLocationsRepository
    val seaFishingLocationsRepository = remember {
        SeaFishingLocationsRepository(
            SeaFishingLocationsApiClient(),
            SeaFishingLocationsDataSource()
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
                    
                    // Add click listener for polygons
                    polygonAnnotationManagerRef.value?.addClickListener(
                        OnPolygonAnnotationClickListener { polygon ->
                            // Find the fishing location associated with this polygon
                            val locationId = polygon.getData()?.asJsonObject?.get("locationId")?.asString
                            locationId?.let { id ->
                                val location = fishingLocations.find { it.name == id }
                                if (location != null) {
                                    selectedFishingLocation = location
                                }
                            }
                            true
                        }
                    )
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
                    .collect { (center, zoom) ->
                        val mapView = mapViewRef.value ?: return@collect
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

                            val manager = annotationManagerRef.value ?: return@collect
                            manager.deleteAll()

                            viewModel.clusters.value.forEach { cluster ->
                                val bitmap = BitmapFactory.decodeResource(mapView.context.resources, R.drawable.yallaimg)
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
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Søk etter norske byer...") },
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
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
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
            // Fishing locations visibility toggle
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        // Toggle visibility state
                        showFishingLocations = !showFishingLocations
                        
                        if (showFishingLocations) {
                            if (!locationsLoaded) {
                                // If button press then load polygons
                                isLoadingFishingLocations = true
                                viewModel.viewModelScope.launch {
                                    try {
                                        val locations = seaFishingLocationsRepository.getFishingLocations()
                                        fishingLocations = locations
                                        locationsLoaded = true
                                        
                                        // Draw polygons after loading completes
                                        polygonAnnotationManagerRef.value?.let { manager ->
                                            drawPolygons(manager, locations)
                                        }
                                    } catch (e: Exception) {
                                        // mark as not loaded so user can try again
                                        locationsLoaded = false
                                        isLoadingFishingLocations = false
                                    } finally {
                                        isLoadingFishingLocations = false
                                    }
                                }
                            } else {
                                // Tegn med allerede hentet data
                                polygonAnnotationManagerRef.value?.let { manager ->
                                    drawPolygons(manager, fishingLocations)
                                }
                            }
                        } else {
                            selectedFishingLocation = null
                            // Clear ALL polygons
                            polygonAnnotationManagerRef.value?.deleteAll()
                        }
                    },
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            if (showFishingLocations) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (!locationsLoaded && !showFishingLocations) 
                                "Vis fiskeplasser (trykk for å laste)" 
                            else if (showFishingLocations) 
                                "Skjul fiskeplasser" 
                            else 
                                "Vis fiskeplasser"
                        )
                    }
                }
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

        // Show location info card if a location is selected
        if (selectedFishingLocation != null) {
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
                    Text(
                        text = selectedFishingLocation?.name ?: "",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Text(
                        text = "Fiskearter: ${selectedFishingLocation?.fishTypes?.joinToString(", ") ?: "Ingen informasjon"}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    // Close button
                    SmallFloatingActionButton(
                        onClick = { selectedFishingLocation = null },
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

        // Loading when fishing locations are being loaded
        if (isLoadingFishingLocations) {
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
                            text = "Laster fiskeplasser...",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

// Function to draw polygons on the map
private fun drawPolygons(polygonManager: PolygonAnnotationManager?, fishingLocations: List<FishingLocation>) {
    polygonManager?.deleteAll() // Clear existing polygons
    
    // Set a higher limit since we're only drawing once - we can handle more polygons
    val maxPolygons = 2000
    val locationsToShow = if (fishingLocations.size > maxPolygons) {
        fishingLocations.take(maxPolygons)
    } else {
        fishingLocations
    }
    
    val colors = listOf(
        "#4D33BBFF", // Blue
        "#4DFF5733", // Red
        "#4D33FF57", // Green
        "#4DFFBB33", // Orange
        "#4D9933FF"  // Purple
    )
    
    val outlineColors = listOf(
        "#33BBFF", // Blue
        "#FF5733", // Red
        "#33FF57", // Green
        "#FFBB33", // Orange
        "#9933FF"  // Purple
    )
    
    // BATCH SIZED POLYGON HANDLING TO AVOID CRASHING
    val batchSize = 50

    locationsToShow.chunked(batchSize).forEachIndexed { batchIndex, locationBatch ->
        val polygonOptionsBatch = mutableListOf<PolygonAnnotationOptions>()
        
        locationBatch.forEachIndexed { locationIndex, location ->
            // Cycle through colors for visual variety
            val colorIndex = (batchIndex * batchSize + locationIndex) % colors.size
            val fillColor = colors[colorIndex]
            val outlineColor = outlineColors[colorIndex]
            
            // Take up to 3 polygons per location to prevent performance issues
            val polygonsToShow = location.polygonPoints.take(3)
            
            polygonsToShow.forEach { polygon ->
                // Convert Pair<Double, Double> to Point because mapbox wants (longitude, latitude)
                val points = polygon.map { (lat, lng) -> 

                    Point.fromLngLat(lng, lat)
                }

                // some of the polygons that we take in are like 3000, and we want this to be simplified for performance
                if (points.size >= 3) { // cant draw polygon with less than 3 points
                    val simplifiedPoints = if (points.size > 50) {
                        points.filterIndexed { i, _ -> i % (points.size / 50 + 1) == 0 }
                    } else {
                        points
                    }
                    
                    val polygonOptions = PolygonAnnotationOptions()
                        .withPoints(listOf(simplifiedPoints)) // MapBox expects a list of list of points for polygons
                        .withFillColor(AndroidColor.parseColor(fillColor))
                        .withFillOpacity(0.5)
                        .withFillOutlineColor(AndroidColor.parseColor(outlineColor))
                    
                    // Store the location ID with the polygon for click handling
                    val jsonObject = com.google.gson.JsonObject()
                    jsonObject.addProperty("locationId", location.name)
                    polygonOptions.withData(jsonObject)
                    
                    polygonOptionsBatch.add(polygonOptions)
                }
            }
        }
        
        // Create all annotations in the batch at once
        if (polygonManager != null && polygonOptionsBatch.isNotEmpty()) {
            polygonManager.create(polygonOptionsBatch)
        }
    }
}


fun boundsToPolygonWKT(bounds: CoordinateBounds): String {
    val sw = bounds.southwest
    val ne = bounds.northeast

    return "POLYGON((" +
            "${sw.longitude()} ${ne.latitude()}, " +
            "${sw.longitude()} ${sw.latitude()}, " +
            "${ne.longitude()} ${sw.latitude()}, " +
            "${ne.longitude()} ${ne.latitude()}, " +
            "${sw.longitude()} ${ne.latitude()}" +
            "))"
}