package no.uio.ifi.in2000.danishah.figmatesting.screens.dashboard

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.MittFiskeLocation
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.TimeSeries
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.WeatherUiState
import no.uio.ifi.in2000.danishah.figmatesting.data.repository.MittFiskeRepository
import no.uio.ifi.in2000.danishah.figmatesting.data.repository.WeatherRepository
import no.uio.ifi.in2000.danishah.figmatesting.data.source.MittFiskeDataSource
import java.time.Duration
import java.time.LocalDateTime
import java.time.OffsetDateTime
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt


class DashboardViewModel : ViewModel() {
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    private val mittFiskeRepository = MittFiskeRepository(MittFiskeDataSource(httpClient))

    private val _weatherData = MutableStateFlow<WeatherData?>(null)
    val weatherData: StateFlow<WeatherData?> = _weatherData.asStateFlow()

    val repository: WeatherRepository = WeatherRepository()
    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState

    init {
        loadWeatherData()
    }

    private fun loadWeatherData() {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            try {
                val data = repository.getWeather(59.9139, 10.7522)
                _uiState.value = WeatherUiState.Success(data)
            } catch (e: Exception) {
                _uiState.value = WeatherUiState.Error("Feil ved henting av værdata: ${e.message}")
            }
        }
    }

    fun getCurrentWeather(): List<TimeSeries> {
        val weather = (uiState.value as? WeatherUiState.Success)?.weather
        if (weather == null) {
            Log.d("DEBUG", "Weather data is null or not loaded yet")
            return emptyList()
        }

        val timeseries = weather.properties.timeseries
        if (timeseries.isEmpty()) return emptyList()

        return listOf(timeseries.first())
    }
    suspend fun hentFiskeplasserForArt(
        brukerLat: Double,
        brukerLon: Double,
        selectedSpecies: String,
        polygonWKT: String,
        pointWKT: String
    ): List<Pair<MittFiskeLocation, Double>> {

        val allLocations = mittFiskeRepository.getLocationsForArea(polygonWKT, pointWKT, 13, 20).getOrDefault(emptyList())
        val normalizedSpecies = selectedSpecies.trim().lowercase()

        val relevantLocations = allLocations.filter { loc ->
            val allFe = loc.locs.flatMap { it.fe ?: emptyList() }
            val fish = extractSupportedFish(allFe)
            Log.d("FISKEFILTER", "Plass: ${loc.name}, Arter: ${fish.joinToString()}, Match: ${normalizedSpecies in fish}")
            normalizedSpecies in fish
        }


        val withDistance = relevantLocations.map { loc ->
            val lat = loc.p.coordinates[1]
            val lon = loc.p.coordinates[0]
            val dist = haversine(brukerLat, brukerLon, lat, lon)
            loc to dist
        }

        return withDistance.sortedBy { it.second }
    }

    private fun extractSupportedFish(fe: List<String>?): List<String> {
        val supported = setOf(
            "torsk", "makrell", "sei", "ørret", "sjøørret", "laks", "gjedde",
            "røye", "hyse", "abbor", "havabbor", "steinbit", "kveite", "rødspette"
        )
        return fe
            ?.flatMap { entry ->
                // Splitt på både komma og mellomrom, fjern tomme strenger
                entry.split(",", " ", "\n", "\t")
                    .map { it.trim().lowercase() }
                    .filter { it.isNotBlank() && it in supported }
            }

            ?.distinct()
            ?: emptyList()
    }

    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371 // jordradius i km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun hentVaerFor(
        lat: Double,
        lon: Double,
        tidspunkt: LocalDateTime,
        repository: WeatherRepository
    ): TimeSeries? {
        return try {
            val response = repository.getWeather(lat, lon)
            val targetTime = tidspunkt.truncatedTo(java.time.temporal.ChronoUnit.HOURS)

            val match = response.properties.timeseries.minByOrNull { ts ->
                val tsTime = OffsetDateTime.parse(ts.time).toLocalDateTime()
                val diff = Duration.between(targetTime, tsTime).abs()
                diff.toMinutes()
            }

            if (match != null) {
                Log.d("VAER-HENTING", "Lat: $lat, Lon: $lon, Tid: $tidspunkt → Match: ${match.time}")
            } else {
                Log.d("VAER-HENTING", "Ingen værdata funnet for $lat,$lon på $tidspunkt")
            }
            match
        } catch (e: Exception) {
            Log.e("VAER-HENTING", "Feil ved henting av vær for $lat,$lon: ${e.message}")
            null
        }
    }


    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return DashboardViewModel() as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

}

data class WeatherData(
    val temperature: Double,
    val condition: String,
    val windSpeed: Double
)
