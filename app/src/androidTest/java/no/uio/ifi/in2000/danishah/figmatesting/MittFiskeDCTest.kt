package no.uio.ifi.in2000.danishah.figmatesting

import com.mapbox.geojson.Point
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.Cluster
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.Loc
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.MittFiskeLocation
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.PointGeometry
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.toPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class MittFiskeDCTest {

    @Test
    fun mittFiskeLocationToPointConvertion() {
        // Create a MittFiskeLocation with known coordinates
        val location = MittFiskeLocation(
            id = "test-id",
            name = "Test Location",
            p = PointGeometry(
                type = "Point",
                coordinates = listOf(10.5, 60.0)  // lon, lat
            ),
            locs = listOf(
                Loc(
                    ca = "test",
                    n = "Test",
                    i = null,
                    u = "test",
                    k = "test",
                    f = "test",
                    fe = listOf("torsk"),
                    de = "test"
                )
            ),
            rating = 3
        )
        
        // Convert to Point
        val point = location.toPoint()
        
        // Verify the conversion is correct
        assertEquals(10.5, point.longitude(), 0.0001)
        assertEquals(60.0, point.latitude(), 0.0001)
    }

    @Test
    fun clusterConstructorInitializesAllFieldsCorrectly() {
        // Create a MittFiskeLocation
        val location = MittFiskeLocation(
            id = "test-id",
            name = "Test Location",
            p = PointGeometry(
                type = "Point",
                coordinates = listOf(10.5, 60.0)
            ),
            locs = emptyList(),
            rating = 3
        )
        
        val center = Point.fromLngLat(10.5, 60.0)

        val cluster = Cluster(
            center = center,
            spots = listOf(location),
            averageRating = 3.5f
        )
        
        // Verify initalization
        assertEquals(center, cluster.center)
        assertEquals(1, cluster.spots.size)
        assertEquals(location, cluster.spots[0])
        cluster.averageRating?.let { assertEquals(3.5f, it, 0.0f) }
    }



    // TEST: Create a PointGeometry with known coordinates
    @Test
    fun pointGeometryInLonLat() {
        val point = PointGeometry(
            type = "Point",
            coordinates = listOf(10.5, 60.0)  // lon, lat
        )
        
        assertEquals(10.5, point.coordinates[0], 0.0)  // lon
        assertEquals(60.0, point.coordinates[1], 0.0)  // lat
    }

    //TEST: Create two identical MittFiskeLocation instances, verify hashes and loc
    @Test
    fun mittFiskeLocationEqualsAndHashCodeWorkCorrectly() {
        val loc1 = MittFiskeLocation(
            id = "test-id",
            name = "Test Location",
            p = PointGeometry(
                type = "Point",
                coordinates = listOf(10.5, 60.0)
            ),
            locs = emptyList(),
            rating = 3
        )
        
        val loc2 = MittFiskeLocation(
            id = "test-id",
            name = "Test Location",
            p = PointGeometry(
                type = "Point",
                coordinates = listOf(10.5, 60.0)
            ),
            locs = emptyList(),
            rating = 3
        )
        
        val loc3 = MittFiskeLocation(
            id = "different-id",
            name = "Different Location",
            p = PointGeometry(
                type = "Point",
                coordinates = listOf(11.0, 61.0)
            ),
            locs = emptyList(),
            rating = 4
        )
        
        assertEquals(loc1, loc2)
        assertEquals(loc1.hashCode(), loc2.hashCode())
        
        assertNotEquals(loc1, loc3)
    }

    //TEST: Create a MittFiskeLocation
    @Test
    fun mittFiskeLocationCorrectCopyModifiedFields() {
        val original = MittFiskeLocation(
            id = "test-id",
            name = "Test Location",
            p = PointGeometry(
                type = "Point",
                coordinates = listOf(10.5, 60.0)
            ),
            locs = emptyList(),
            rating = 3
        )
        
        val copy = original.copy(rating = 4)
        
        assertEquals(4, copy.rating)
        assertEquals(original.id, copy.id)
        assertEquals(original.name, copy.name)
        assertEquals(original.p, copy.p)
        assertEquals(original.locs, copy.locs)
    }
} 