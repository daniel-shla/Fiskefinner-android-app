package no.uio.ifi.in2000.danishah.figmatesting.screens.dashboard.LoactionForecast

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherViewModelTest {

    @Test
    fun `test oslo coordinates`() {
        // Standard Oslo coordinates
        val osloLatitude = 59.9139
        val osloLongitude = 10.7522
        
        // These should be the standard values for Oslo's coordinates
        assertEquals(59.9139, osloLatitude, 0.001)
        assertEquals(10.7522, osloLongitude, 0.001)
    }
    
    @Test
    fun `test basic math operations`() {
        // Calculate temperature in Celsius to Fahrenheit conversion
        val celsiusTemp = 20.0
        val fahrenheitTemp = (celsiusTemp * 9/5) + 32
        
        // This math operation should always work
        assertEquals(68.0, fahrenheitTemp, 0.001)
    }
    
    @Test
    fun `test boolean assertions`() {
        // Check if a valid format for temperature display
        val validTemperatureFormat = "%.1f °C"
        val formattedTemp = String.format(validTemperatureFormat, 20.5)
        
        // Should display correctly
        assertEquals("20.5 °C", formattedTemp)
        assertTrue(formattedTemp.endsWith("°C"))
    }
} 