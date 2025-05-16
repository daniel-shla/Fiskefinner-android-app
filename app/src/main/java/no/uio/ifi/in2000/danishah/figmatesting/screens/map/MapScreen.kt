package no.uio.ifi.in2000.danishah.figmatesting.screens.map

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.QuestionMark
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
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
import com.mapbox.maps.plugin.gestures.gestures
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.Cluster
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.FishSpeciesData
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.MittFiskeLocation
import no.uio.ifi.in2000.danishah.figmatesting.screens.fishselection.FishSpeciesViewModel
import no.uio.ifi.in2000.danishah.figmatesting.screens.map.cards.ClusterOverviewCard
import no.uio.ifi.in2000.danishah.figmatesting.screens.map.cards.LocationInfoCard
import no.uio.ifi.in2000.danishah.figmatesting.screens.map.cards.SearchResultsCard
import no.uio.ifi.in2000.danishah.figmatesting.screens.map.components.MapHelpDialog
import no.uio.ifi.in2000.danishah.figmatesting.screens.map.components.SpeciesLegend
import no.uio.ifi.in2000.danishah.figmatesting.screens.map.components.getColorForSpecies
import no.uio.ifi.in2000.danishah.figmatesting.screens.map.mittFiske.MittFiskeViewModel
import android.graphics.Color as AndroidColor


@OptIn(FlowPreview::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel,
    fishSpeciesViewModel: FishSpeciesViewModel,
    mittFiskeViewModel: MittFiskeViewModel
)
 {
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    val showHelpDialog = remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Du må gi stedstillatelse for å bruke denne funksjonen", Toast.LENGTH_LONG).show()
        }
    }


    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSearchActive by viewModel.isSearchActive.collectAsState()
    val mapCenter by viewModel.mapCenter.collectAsState()
    val zoomLevel by viewModel.zoomLevel.collectAsState()
    val showMinCharsHint by viewModel.showMinCharsHint.collectAsState()

    val mapViewRef = remember { mutableStateOf<MapView?>(null) }
    val currentBounds = remember { mutableStateOf<CoordinateBounds?>(null) }
    val annotationManagerRef = remember { mutableStateOf<PointAnnotationManager?>(null) }
    // New state for polygon annotation manager
    val polygonAnnotationManagerRef = remember { mutableStateOf<PolygonAnnotationManager?>(null) }

    // Using the passed FishSpeciesViewModel instead of creating a new one
    val speciesStates by fishSpeciesViewModel.speciesStates.collectAsState()
    val isLoadingSpecies by fishSpeciesViewModel.isLoading.collectAsState()


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




    val mittFiskeState by mittFiskeViewModel.uiState.collectAsState()

     LaunchedEffect(mittFiskeState.isLoading) {
         if (!mittFiskeState.isLoading && mittFiskeState.locations.any { it.rating != null }) {

             mittFiskeViewModel.preloadBitmaps(context)

             delay(300)

             mapViewRef.value?.let { mapView ->
                 val camera = mapViewportState.cameraState ?: return@let
                 val bounds = mapView.mapboxMap.coordinateBoundsForCamera(
                     CameraOptions.Builder()
                         .center(camera.center)
                         .zoom(camera.zoom)
                         .build()
                 )
                 currentBounds.value = bounds

                 val visible = mittFiskeViewModel.filterLocationsInBounds(
                     mittFiskeState.locations,
                     bounds
                 )

                 viewModel.updateClusters(visible, camera.zoom)
                 viewModel.triggerDraw() // <- denne må kalles manuelt første gang
             }
         }
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

    // KEEP NULLCHECKS AS THEY ARE
    remember(mapViewportState.cameraState?.center, mapViewportState.cameraState?.zoom) {
        mapViewportState.cameraState?.center?.let { center ->
            mapViewportState.cameraState!!.zoom.let { zoom ->
                viewModel.updateMapPosition(center, zoom)
            }
        }
        true
    }

    // Effect to draw fish species polygons when species states change
     LaunchedEffect(speciesStates, isLoadingSpecies) {        // Get all enabled species
         if (isLoadingSpecies) return@LaunchedEffect
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
            if (state.species.ratedPolygons.isNotEmpty()) {
                polygonAnnotationManagerRef.value?.let { manager ->
                    drawFishSpeciesPolygons(
                        manager,
                        state.species,
                        getColorForSpecies(state.species.scientificName)
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
                if (state.species.ratedPolygons.isNotEmpty()) {
                    polygonAnnotationManagerRef.value?.let { manager ->
                        drawFishSpeciesPolygons(
                            manager,
                            state.species,
                            getColorForSpecies(state.species.scientificName)
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

            LaunchedEffect(mittFiskeState.locations, mittFiskeState.isLoading) {
                if (!mittFiskeState.isLoading && mittFiskeState.locations.isNotEmpty()) {
                    val mapView = mapViewRef.value ?: return@LaunchedEffect
                    val bounds = mapView.mapboxMap.coordinateBoundsForCamera(
                        CameraOptions.Builder()
                            .center(mapViewportState.cameraState?.center)
                            .zoom(mapViewportState.cameraState?.zoom)
                            .build()
                    )
                    currentBounds.value = bounds

                    val visible = mittFiskeViewModel.filterLocationsInBounds(
                        mittFiskeState.locations,
                        bounds
                    )


                    viewModel.updateClusters(visible, zoomLevel)
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

                val bounds = mapView.mapboxMap.coordinateBoundsForCamera(
                    CameraOptions.Builder()
                        .center(mapViewportState.cameraState?.center)
                        .zoom(mapViewportState.cameraState?.zoom)
                        .build()
                )
                currentBounds.value = bounds

                // Legg til klikk på kart for å lukke åpne kort
                mapView.gestures.addOnMapClickListener {
                    selectedLocation.value = null
                    selectedCluster.value = null
                    false // behold annen default-oppførsel
                }
            }



            LaunchedEffect(Unit) {
                snapshotFlow { mapViewportState.cameraState }
                    .filterNotNull()
                    .map { cameraState -> cameraState.center to cameraState.zoom }
                    .distinctUntilChanged()
                    .debounce(300)//Can be adjustes
                    .collect { (center, zoom) ->
                        withContext(Dispatchers.Default) {
                            val mapView = mapViewRef.value ?: return@withContext
                            val bounds = withContext(Dispatchers.Main) { //Moved coordinateBoundsForCamera to support Mapbox backend sensitivity
                                mapView.mapboxMap.coordinateBoundsForCamera(
                                    CameraOptions.Builder().center(center).zoom(zoom).build()
                                )
                            }

                            val locations = mittFiskeState.locations
                            val visibleLocations = mittFiskeViewModel.filterLocationsInBounds(locations, bounds)

                            if (locations.isNotEmpty()) {
                                viewModel.updateClusters(visibleLocations, zoom)
                                viewModel.triggerDraw()

                                //Imporved annotations
                                val newAnnotations = viewModel.clusters.value.map { cluster ->
                                    val isCluster = cluster.spots.size > 1
                                    val bitmap = if (isCluster) {
                                        mittFiskeViewModel.getBitmapForCluster(cluster.averageRating)
                                    } else {
                                        mittFiskeViewModel.getBitmapForLocation(cluster.averageRating)
                                    }

                                    val options = PointAnnotationOptions()
                                        .withPoint(cluster.center)
                                        .withIconImage(bitmap)
                                        .withIconSize(0.05)
                                        .withTextOffset(listOf(0.0, -2.0))
                                        .withTextSize(12.0)

                                    options to cluster.spots.first()
                                }

                                //Only UI on the Main here
                                withContext(Dispatchers.Main) {
                                    val manager = annotationManagerRef.value ?: return@withContext
                                    manager.deleteAll()

                                    newAnnotations.forEach { (options, location) ->
                                        val annotation = manager.create(options)
                                        annotationToLocation[annotation.id] = location
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

        if (speciesStates.values.any { it.isEnabled && it.isLoaded }) {
            SpeciesLegend(
                speciesStates = speciesStates,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 90.dp, start = 16.dp) // Position it below the search bar, to the left
            )
        }

        selectedCluster.value?.let {
            Box(modifier = Modifier.fillMaxSize()) {
                ClusterOverviewCard(
                    cluster = selectedCluster.value!!,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 140.dp),
                    onClose = { selectedCluster.value = null}
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
                    .height(56.dp),
                placeholder = {
                    Text(
                        "Søk...",
                        style = MaterialTheme.typography.bodySmall
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
                    focusedContainerColor = Color.White,
                    unfocusedTextColor = Color.Black,
                    focusedTextColor = Color.Black,
                    cursorColor = Color.Black
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

            Spacer(modifier = Modifier.height(8.dp))


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
                            viewModel.navigateToLocation(suggestion) //Support onclick suggestion
                            viewModel.setSearchActive(false)         //Hide results
                            focusManager.clearFocus()                //Hide keyboard
                        }

                    )
                }
            }

            selectedLocation.value?.let { location ->
                LocationInfoCard(
                    location = location,
                    onClose  = { selectedLocation.value = null },
                    modifier = Modifier
                        .align(Alignment.TopCenter)   // Same as cluster
                        .padding(top = 110.dp)
                )
            }

        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            Spacer(modifier = Modifier.height(8.dp))

            MapControls(
                viewModel = viewModel,
                context = context,
                launcher = launcher
            )
        }
        if (mittFiskeState.isLoading) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .align(Alignment.Center)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    strokeWidth = 4.dp
                )
                Text(
                    "Laster fiskeplasser …",
                    style  = MaterialTheme.typography.bodyMedium,
                    color  = Color.White,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(top = 60.dp)
                )
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

        // Help button in bottom left
        SmallFloatingActionButton(
            onClick = { showHelpDialog.value = true },
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .size(40.dp)
        ) {
            Icon(Icons.Default.QuestionMark, contentDescription = "Hjelp")
        }

        // Species legend below search bar (not at bottom-right anymore)

    }

    // Show help dialog when needed
    if (showHelpDialog.value) {
        MapHelpDialog(onDismiss = { showHelpDialog.value = false })
    }
}

// Function to draw fish species polygons
private fun drawFishSpeciesPolygons(
    polygonManager: PolygonAnnotationManager?,
    fishSpecies: FishSpeciesData,
    color: Color
) {

    if (polygonManager == null || fishSpecies.ratedPolygons.isEmpty()) return

    val maxPolygons = 2000
    val polygonsToShow = if (fishSpecies.ratedPolygons.size > maxPolygons) {
        val step = fishSpecies.ratedPolygons.size / maxPolygons
        fishSpecies.ratedPolygons.filterIndexed { index, _ -> index % step == 0 }.take(maxPolygons)
    } else {
        fishSpecies.ratedPolygons
    }

    val colorWithoutAlpha = "#${color.toArgb().toHexString().substring(2)}"
    val fillColor = AndroidColor.parseColor(colorWithoutAlpha)

    val outlineColor = AndroidColor.BLACK

    var firstPolygonFirstPoint: Point? = null

    val batchSize = 100
    val batches = polygonsToShow.chunked(batchSize)

    batches.forEach { batchPolygons ->
        val batchAnnotations = batchPolygons.mapNotNull { polygon ->
            if (polygon.points.size < 3) return@mapNotNull null
            val rating = polygon.rating
            val fillOpacity = rating * 0.25
            val points = polygon.points.map { (lat, lng) -> Point.fromLngLat(lng, lat) }
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

        polygonManager.create(batchAnnotations)
    }

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


