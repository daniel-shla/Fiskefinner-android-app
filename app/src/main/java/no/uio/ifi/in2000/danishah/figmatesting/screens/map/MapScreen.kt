package no.uio.ifi.in2000.danishah.figmatesting.screens.map

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import io.ktor.client.HttpClient
import no.uio.ifi.in2000.danishah.figmatesting.data.repository.MittFiskeRepository
import no.uio.ifi.in2000.danishah.figmatesting.data.source.LocationDataSource
import no.uio.ifi.in2000.danishah.figmatesting.data.source.MittFiskeDataSource
import no.uio.ifi.in2000.danishah.figmatesting.screens.mittFiske.MittFiskeViewModel
import no.uio.ifi.in2000.danishah.figmatesting.screens.mittFiske.MittFiskeViewModelFactory
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.plugin.Plugin
import com.mapbox.maps.plugin.annotation.AnnotationPlugin
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import kotlinx.coroutines.delay
import no.uio.ifi.in2000.danishah.figmatesting.R


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

    LaunchedEffect(Unit) {
        mittFiskeViewModel.loadLocations(
            /*
-26.893700772506584 82.33641809270495,-26.893700772506584 23.91350077509478,37.266455477493416 23.91350077509478,37.266455477493416 82.33641809270495,-26.893700772506584 82.33641809270495
10.52400614376138 60.07455470863071,10.52400614376138%2059.81974768311581,10.774631754112942 59.81974768311581,10.774631754112942 60.07455470863071,10.52400614376138 60.07455470863071
5.302921548204229 62.18340318067006,5.302921548204229 58.13876707969069,9.312931313829228 58.13876707969069,9.312931313829228 62.18340318067006,5.302921548204229 62.18340318067006
6.505125601910762 59.89990727321326,6.505125601910762 58.86343183709712,7.507628043317012 58.86343183709712,7.507628043317012 59.89990727321326,6.505125601910762 59.89990727321326
             */


            polygonWKT = "POLYGON((6.505125601910762 59.89990727321326,6.505125601910762 58.86343183709712,7.507628043317012 58.86343183709712,7.507628043317012 59.89990727321326,6.505125601910762 59.89990727321326))",

            pointWKT = "POINT(10.7124 59.9797)"
        )
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
                    Log.d("MittFiske", "loading")

                }
                if (mittFiskeState.locations.isNotEmpty()) {
                    Log.d("MittFiske", "Klar for tegning av ${mittFiskeState.locations.size} punkter")
                    viewModel.triggerDraw()
                }
            }

            if (drawPoints && mittFiskeState.locations.isNotEmpty()) {
                MapEffect(mapViewportState) { mapView ->
                    val annotationPlugin = mapView.getPlugin(Plugin.MAPBOX_ANNOTATION_PLUGIN_ID) as? AnnotationPlugin
                    val pointAnnotationManager = annotationPlugin?.createPointAnnotationManager()
                    pointAnnotationManager?.deleteAll()

                    Log.d("MittFiske", "Tegner ${mittFiskeState.locations.size} punkter")

                    mittFiskeState.locations.forEach { location ->
                        try {
                            val lon = location.p.coordinates[0]
                            val lat = location.p.coordinates[1]
                            val point = Point.fromLngLat(lon, lat)

                            val context = mapView.context
                            val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.img)


                            val options = PointAnnotationOptions()
                                .withPoint(point)
                                .withIconImage(bitmap)
                                .withIconSize(0.1)  // Make points larger

                            pointAnnotationManager?.create(options)
                            Log.d("MittFiske", "nå skal det være  med punkt ${lon}, ${lat} = ${point}")

                        } catch (e: Exception) {
                            Log.e("MittFiske", "Feil med punkt: ${e.message}")
                        }
                    }
                }
            }

        }

        // Search bar container (Jetpack Compose semantikk)
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
                            // Use the searchAndNavigate function to go to selected or searched location
                            viewModel.searchAndNavigate(searchQuery)
                            // Hide search suggestions
                            viewModel.setSearchActive(false)
                        }
                    }) {
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
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
                            // Hide search suggestions
                            viewModel.setSearchActive(false)
                        }
                    }
                )
            )

            // Show search results as a dropdown
            if (isSearchActive) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 56.dp) // below the search bar
                        .zIndex(1f), // Ensure it appears above other content
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = 4.dp
                ) {
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    } else if (searchResults.isNotEmpty()) {
                        // Show search suggestions
                        LazyColumn(
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .heightIn(max = 300.dp) // maks 4 suggestions, ikke endre denne
                        ) {
                            items(searchResults) { suggestion ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            // Select the suggestion but don't navigate yet
                                            viewModel.selectSuggestion(suggestion)
                                            // Keep the dropdown open until user clicks the button
                                        }
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Show appropriate icon based on feature type
                                    val icon = when {
                                        suggestion.maki != null -> Icons.Default.Place
                                        suggestion.featureType == "address" -> Icons.Default.LocationOn
                                        else -> Icons.Default.Place
                                    }

                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column {
                                        Text(
                                            text = suggestion.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium
                                        )

                                        // Show address or place information if available
                                        suggestion.fullAddress?.let {
                                            Text(
                                                text = it,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        } ?: suggestion.placeFormatted?.let {
                                            Text(
                                                text = it,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else if (showMinCharsHint) {
                        Text(
                            text = "Skriv minst 3 tegn for å søke",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else if (searchQuery.length >= 3) {
                        // Show "No results" message
                        Text(
                            text = "Ingen steder funnet som matcher '$searchQuery'",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
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
    }
}
