package no.uio.ifi.in2000.danishah.figmatesting.screens.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import no.uio.ifi.in2000.danishah.figmatesting.screens.dashboard.UserLocation

@Composable
fun MapControls(
    viewModel: MapViewModel,
    context: Context,
    mapViewportState: MapViewportState,
    launcher: ManagedActivityResultLauncher<String, Boolean>
) {
    SmallFloatingActionButton(
        onClick = { viewModel.zoomIn() },
        shape = CircleShape,
        modifier = Modifier.size(40.dp)
    ) {
        Icon(Icons.Default.Add, contentDescription = "Zoom inn")
    }

    Spacer(modifier = Modifier.height(8.dp))

    SmallFloatingActionButton(
        onClick = { viewModel.zoomOut() },
        shape = CircleShape,
        modifier = Modifier.size(40.dp)
    ) {
        Icon(Icons.Default.Remove, contentDescription = "Zoom ut")
    }

    Spacer(modifier = Modifier.height(8.dp))

    SmallFloatingActionButton(
        onClick = {
            val permission = Manifest.permission.ACCESS_FINE_LOCATION
            val hasPermission = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            val userPoint = Point.fromLngLat(location.longitude, location.latitude)
                            UserLocation.update(userPoint)
                            viewModel.navigateToPoint(userPoint)
                        } else {
                            Toast.makeText(context, "Fant ikke gjeldende posisjon", Toast.LENGTH_SHORT).show()
                        }
                    }

            } else {
                launcher.launch(permission)
            }
        },
        shape = CircleShape,
        modifier = Modifier.size(40.dp)
    ) {
        Icon(Icons.Default.MyLocation, contentDescription = "Min posisjon")
    }
}
