package no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses

import org.junit.Assert.assertEquals
import org.junit.Test

class TrainingDataTest {

    @Test
    fun training() {
        val trainingData = TrainingData(
            temperature = 15.5f,
            windSpeed = 5.2f,
            precipitation = 0.0f,
            airPressure = 1013.25f,
            cloudCover = 25.0f,
            timeOfDay = 14.0f,
            season = 2.0f,
            latitude = 60.0f,
            longitude = 10.5f,
            speciesId = 0f
        )
        
        // Verify initialisation of fields
        assertEquals(15.5f, trainingData.temperature, 0.0f)
        assertEquals(5.2f, trainingData.windSpeed, 0.0f)
        assertEquals(0.0f, trainingData.precipitation, 0.0f)
        assertEquals(1013.25f, trainingData.airPressure, 0.0f)
        assertEquals(25.0f, trainingData.cloudCover, 0.0f)
        assertEquals(14.0f, trainingData.timeOfDay, 0.0f)
        assertEquals(2.0f, trainingData.season, 0.0f)
        assertEquals(60.0f, trainingData.latitude, 0.0f)
        assertEquals(10.5f, trainingData.longitude, 0.0f)
        assertEquals(0f, trainingData.speciesId, 0.0f)
    }
    
    @Test
    fun checkEqualsAndHashCode() {
        // Create two identical TrainingData instances
        val data1 = TrainingData(
            temperature = 15.5f,
            windSpeed = 5.2f,
            precipitation = 0.0f,
            airPressure = 1013.25f,
            cloudCover = 25.0f,
            timeOfDay = 14.0f,
            season = 2.0f,
            latitude = 60.0f,
            longitude = 10.5f,
            speciesId = 0f
        )
        
        val data2 = TrainingData(
            temperature = 15.5f,
            windSpeed = 5.2f,
            precipitation = 0.0f,
            airPressure = 1013.25f,
            cloudCover = 25.0f,
            timeOfDay = 14.0f,
            season = 2.0f,
            latitude = 60.0f,
            longitude = 10.5f,
            speciesId = 0f
        )
        
        // Create a different TrainingData instance
        val data3 = TrainingData(
            temperature = 20.0f,
            windSpeed = 5.2f,
            precipitation = 0.0f,
            airPressure = 1013.25f,
            cloudCover = 25.0f,
            timeOfDay = 14.0f,
            season = 2.0f,
            latitude = 60.0f,
            longitude = 10.5f,
            speciesId = 0f
        )
        
        // Verify that equals and hashcode work as intended
        assertEquals(data1, data2)
        assertEquals(data1.hashCode(), data2.hashCode())
        
        // Verify different objects are not equal
        assert(data1 != data3)
    }
    
    @Test
    fun checkTrainingDataCopy() {
        val original = TrainingData(
            temperature = 15.5f,
            windSpeed = 5.2f,
            precipitation = 0.0f,
            airPressure = 1013.25f,
            cloudCover = 25.0f,
            timeOfDay = 14.0f,
            season = 2.0f,
            latitude = 60.0f,
            longitude = 10.5f,
            speciesId = 0f
        )
        
        // Create a copy with one modified field
        val copy = original.copy(temperature = 20.0f)
        
        // Verify the copy has the modified field and all other fields remain the same
        assertEquals(20.0f, copy.temperature, 0.0f)
        assertEquals(original.windSpeed, copy.windSpeed, 0.0f)
        assertEquals(original.precipitation, copy.precipitation, 0.0f)
        assertEquals(original.airPressure, copy.airPressure, 0.0f)
        assertEquals(original.cloudCover, copy.cloudCover, 0.0f)
        assertEquals(original.timeOfDay, copy.timeOfDay, 0.0f)
        assertEquals(original.season, copy.season, 0.0f)
        assertEquals(original.latitude, copy.latitude, 0.0f)
        assertEquals(original.longitude, copy.longitude, 0.0f)
        assertEquals(original.speciesId, copy.speciesId, 0.0f)
    }
} 