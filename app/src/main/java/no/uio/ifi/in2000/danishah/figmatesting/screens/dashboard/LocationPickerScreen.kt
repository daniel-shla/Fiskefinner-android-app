package no.uio.ifi.in2000.danishah.figmatesting.screens.dashboard

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createCircleAnnotationManager
import com.mapbox.maps.plugin.gestures.gestures

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerScreen(navController: NavController) {
    val context = LocalContext.current
    var selectedPoint by remember { mutableStateOf<Point?>(null) }
    var mapView: MapView? = null
    var circleAnnotationManager: CircleAnnotationManager? = null

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Velg lokasjon",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 0.dp)
                    )
                },

                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Tilbake")
                    }
                },
                actions = {
                    TextButton(enabled = selectedPoint != null, onClick = {
                        selectedPoint?.let {
                            navController.previousBackStackEntry?.savedStateHandle?.set("selectedLocation", it)
                            navController.popBackStack()
                        }
                    }) {
                        Text("Velg")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            AndroidView(factory = { ctx ->
                mapView = MapView(ctx)
                mapView?.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                mapView?.mapboxMap?.loadStyleUri(Style.MAPBOX_STREETS) {
                    val annotations = mapView!!.annotations
                    circleAnnotationManager = annotations.createCircleAnnotationManager()

                    mapView!!.gestures.addOnMapClickListener { point ->
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

                    mapView!!.getMapboxMap().setCamera(
                        CameraOptions.Builder()
                            .center(Point.fromLngLat(10.75, 59.91))
                            .zoom(5.0)
                            .build()
                    )
                    mapView!!.gestures.apply {
                        pinchToZoomEnabled = true
                        scrollEnabled = true
                        rotateEnabled = false
                    }

                }
                mapView!!
            }, update = {})
        }
    }
}