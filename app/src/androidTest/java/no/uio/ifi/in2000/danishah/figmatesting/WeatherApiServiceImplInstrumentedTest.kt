package no.uio.ifi.in2000.danishah.figmatesting

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import no.uio.ifi.in2000.danishah.figmatesting.data.apiclient.WeatherApiServiceImpl
import no.uio.ifi.in2000.danishah.figmatesting.data.repository.WeatherRepository
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WeatherApiServiceImplInstrumentedTest {

    @Test
    fun getWeather_returnsValidParsedData() = runBlocking {
        val repo = WeatherRepository()
        val response = repo.getWeather(60.0, 10.0)

        val first = response.properties.timeseries.first()
        val details = first.data.instant.details

        assertTrue(details.air_temperature > -50 && details.air_temperature < 50)
        assertTrue(details.wind_speed >= 0)
        assertTrue(details.air_pressure_at_sea_level > 900)
    }


}
