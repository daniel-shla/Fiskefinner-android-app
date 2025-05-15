package no.uio.ifi.in2000.danishah.figmatesting.screens.dashboard

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createCircleAnnotationManager
import com.mapbox.maps.plugin.gestures.gestures


@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerScreen(navController: NavController) {
    var selectedPoint by remember { mutableStateOf<Point?>(null) }
    var mapView: MapView? = null
    var circleAnnotationManager: CircleAnnotationManager? = null


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors   = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),

                title = {
                    Text(
                        "Velg lokasjon for søk",
                        style = MaterialTheme.typography.titleMedium
                    )
                },

                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Tilbake")
                    }
                },

                actions = {
                    TextButton(
                        enabled = selectedPoint != null,
                        onClick = {
                            selectedPoint?.let {
                                navController.previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.set("selectedLocation", it)
                                navController.popBackStack()
                            }
                        }
                    ) {
                        Text(
                            "VELG",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            )
        }
    )

    { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AndroidView(
                modifier = Modifier
                    .matchParentSize()
                    .zIndex(-1f),


                factory = { ctx ->
                    mapView = MapView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        mapboxMap.loadStyleUri(Style.MAPBOX_STREETS) {
                            val annotations = annotations
                            circleAnnotationManager = annotations.createCircleAnnotationManager()

                            gestures.addOnMapClickListener { point ->
                                selectedPoint = point
                                circleAnnotationManager?.deleteAll()
                                circleAnnotationManager?.create(
                                    CircleAnnotationOptions()
                                        .withPoint(point)
                                        .withCircleRadius(8.0)
                                        .withCircleColor("#0088FF")
                                )
                                true
                            }

                            getMapboxMap().setCamera(
                                CameraOptions.Builder()
                                    .center(Point.fromLngLat(10.75, 59.91))
                                    .zoom(5.0)
                                    .build()
                            )
                            gestures.apply {
                                pinchToZoomEnabled = true
                                scrollEnabled     = true
                                rotateEnabled     = false
                            }
                        }
                    }
                    mapView!!
            }, update = {})
        }
    }
}
