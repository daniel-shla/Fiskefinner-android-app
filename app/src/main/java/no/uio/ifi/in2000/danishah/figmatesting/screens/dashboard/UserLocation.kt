package no.uio.ifi.in2000.danishah.figmatesting.screens.dashboard

import com.mapbox.geojson.Point
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object UserLocation {
    /**  Sist kjente posisjon som brukeren har samtykket til å dele  */
    private val _current = MutableStateFlow<Point?>(null)
    val current = _current.asStateFlow()

    fun update(point: Point) {
        _current.value = point
    }
}
