package no.uio.ifi.in2000.danishah.figmatesting.screens.dashboard

import kotlinx.coroutines.withContext
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.mapbox.geojson.Point
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.danishah.figmatesting.R
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.MittFiskeLocation
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.TrainingData
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.WeatherUiState
import no.uio.ifi.in2000.danishah.figmatesting.ml.FishPredictor
import no.uio.ifi.in2000.danishah.figmatesting.ml.SpeciesMapper
import no.uio.ifi.in2000.danishah.figmatesting.screens.dashboard.cards.WeatherCard
import no.uio.ifi.in2000.danishah.figmatesting.screens.dashboard.components.DashboardHelpDialog
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory),
    navController: NavController
) {
    val uiState             by viewModel.uiState.collectAsState()
    val usingUserLocation   by viewModel.usingUserLocation.collectAsState()
    val scrollState         = rememberScrollState()
    val showHelpDialog      = remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            when (uiState) {
                is WeatherUiState.Loading -> {
                    Text("Laster værdata...")
                }
                is WeatherUiState.Error -> {
                    val error = (uiState as WeatherUiState.Error).message
                    Text("Feil: $error")
                }
                is WeatherUiState.Success -> {
                    val weather = viewModel.getCurrentWeather()
                    if (weather.isNotEmpty()) {
                        val label = if (usingUserLocation) "Din posisjon" else "Oslo"
                        WeatherCard(weather.first(), label)
                    } else {
                        Text("Ingen værdata tilgjengelig.")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            FishTripPlannerSection(navController = navController)
        }

        // help button in the bottom left corner
        SmallFloatingActionButton(
            onClick = { showHelpDialog.value = true },
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .size(40.dp)
        ) {
            Icon(Icons.Default.QuestionMark, contentDescription = "Hjelp")
        }
    }

    if (showHelpDialog.value) {
        DashboardHelpDialog(onDismiss = { showHelpDialog.value = false })
    }
}

@RequiresApi(Build.VERSION_CODES.O)
val LocalDateTimeSaver = Saver<LocalDateTime?, String>(
    save = { it?.toString() ?: "" },
    restore = { if (it.isNotEmpty()) LocalDateTime.parse(it) else null }
)


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FishTripPlannerSection(navController: NavController) {
    val currentBackStackEntry = navController.currentBackStackEntryAsState().value
    val savedStateHandle = currentBackStackEntry?.savedStateHandle
    val coroutineScope = rememberCoroutineScope()
    var canNavigate by remember { mutableStateOf(true) }

    val selectedSpeciesIdLive = savedStateHandle?.getLiveData<Int>("selectedSpeciesId")
    val selectedLocationLive = savedStateHandle?.getLiveData<Point>("selectedLocation")

    var selectedSpeciesId by remember { mutableStateOf<Int?>(null) }
    var selectedLocation by remember { mutableStateOf<Point?>(null) }
    var selectedDateTime by rememberSaveable(stateSaver = LocalDateTimeSaver) {
        mutableStateOf(
            null)
    }
    var radiusKm by rememberSaveable { mutableIntStateOf(100) } // standard
    val radiusLive = savedStateHandle?.getLiveData<Int>("radiusKm")
    LaunchedEffect(radiusLive) {
        radiusLive?.observeForever { v -> radiusKm = v }
    }
    var showRadiusDialog by remember { mutableStateOf(false) }


    val canReturn = savedStateHandle?.get<Boolean>("canNavigateBackToSpeciesPicker") == true

    LaunchedEffect(canReturn) {
        if (canReturn) {
            canNavigate = true
            savedStateHandle?.set("canNavigateBackToSpeciesPicker", false)
        }
    }

    val context = LocalContext.current
    val dashboardViewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory)

    var distanceSorted by remember { mutableStateOf<List<Pair<MittFiskeLocation, Double>>>(emptyList()) }
    var aiSorted by remember { mutableStateOf<List<Pair<MittFiskeLocation, Float>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var distanceMap by remember {
        mutableStateOf<Map<MittFiskeLocation, Double>>(emptyMap())
    }


    LaunchedEffect(selectedSpeciesIdLive) {
        selectedSpeciesIdLive?.observeForever { id ->
            selectedSpeciesId = id
        }
    }

    LaunchedEffect(selectedLocationLive) {
        selectedLocationLive?.observeForever { point ->
            selectedLocation = point
        }
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text(
                    "Fiskeplanlegger",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Tilbakestill",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .clickable {
                            selectedSpeciesId = null
                            selectedDateTime = null
                            selectedLocation = null
                            distanceSorted = emptyList()
                            aiSorted = emptyList()
                            savedStateHandle?.remove<Int>("selectedSpeciesId")
                            savedStateHandle?.remove<Point>("selectedLocation")
                            savedStateHandle?.remove<String>("selectedDateTime")
                        }
                        .padding(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }



            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            )
            {
                Spacer(modifier = Modifier.height(24.dp))

                PlannerInputBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp),
                    iconRes = R.drawable.fishbreh,
                    label = selectedSpeciesId?.let {
                        SpeciesMapper.getName(it)?.replaceFirstChar(Char::uppercase)
                    } ?: ""
                ) {
                    if (canNavigate) {
                        canNavigate = false
                        savedStateHandle?.set("speciesNavigationInProgress", true)

                        coroutineScope.launch {
                            navController.navigate("fish_species_picker") {
                                launchSingleTop = true
                            }
                            delay(2000) // buffer
                            canNavigate = true
                        }
                    }
                }

                PlannerInputBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp),
                    iconRes = R.drawable.clock,
                    label   = selectedDateTime
                        ?.format(DateTimeFormatter.ofPattern("dd.MM HH:mm"))
                        ?: ""
                ) {
                    val today = Calendar.getInstance()
                    val maxDateMillis = System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000

                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    selectedDateTime = LocalDateTime.of(
                                        LocalDate.of(year, month + 1, dayOfMonth),
                                        LocalTime.of(hour, minute)
                                    )
                                    savedStateHandle?.set("selectedDateTime", selectedDateTime!!.toString())
                                },
                                today.get(Calendar.HOUR_OF_DAY),
                                today.get(Calendar.MINUTE),
                                true
                            ).show()
                        },
                        today.get(Calendar.YEAR),
                        today.get(Calendar.MONTH),
                        today.get(Calendar.DAY_OF_MONTH)
                    ).apply {
                        datePicker.maxDate = maxDateMillis
                        datePicker.minDate = System.currentTimeMillis()
                    }.show()
                }

                PlannerInputBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp),
                    iconRes = R.drawable.pin,
                    label = selectedLocation?.let { "Valgt" } ?: ""
                ) {
                    if (canNavigate) {
                        canNavigate = false
                        coroutineScope.launch {
                            Log.d("NAVIGATION", "Navigating to location_picker from Dashboard")
                            navController.navigate("location_picker") {
                                launchSingleTop = true
                                restoreState = false
                            }
                            delay(500)
                            canNavigate = true
                        }
                    }
                }
                //Select your radius
                PlannerInputBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp),
                    iconRes = R.drawable.distance,
                    label   = "$radiusKm km"
                ) {
                    if (!showRadiusDialog) { // simple «double-tap» protection
                        showRadiusDialog = true
                    }
                }


                if (showRadiusDialog) {
                    ShowRadiusDialog(
                        current   = radiusKm,
                        onConfirm = { newKm ->
                            radiusKm = newKm
                            savedStateHandle?.set("radiusKm", newKm)
                            showRadiusDialog = false
                        },
                        onDismiss = { showRadiusDialog = false }
                    )
                }
            }

            if (selectedSpeciesId != null && selectedDateTime != null && selectedLocation != null) {

                LaunchedEffect(selectedSpeciesId, selectedLocation, selectedDateTime, radiusKm) {
                    if (selectedSpeciesId != null && selectedLocation != null && selectedDateTime != null) {
                        isLoading = true
                        delay(300) //Small debounce

                        val selectedSpeciesName =
                            SpeciesMapper.getName(selectedSpeciesId!!)?.lowercase() ?: "torsk"
                        val userLat = selectedLocation!!.latitude()
                        val userLon = selectedLocation!!.longitude()
                        val polygonWKT =
                            "POLYGON((4.0 71.5, 4.0 57.9, 31.5 57.9, 31.5 71.5, 4.0 71.5))"
                        val pointWKT = "POINT(15.0 64.0)"

                        val (sortedByDistance, sortedByAI) = withContext(Dispatchers.Default) {

                            //Fetch all places for the chosen species
                            val result = dashboardViewModel.getFishSpotsForSpecies(
                                userLat      = userLat,
                                userLon      = userLon,
                                selectedSpecies = selectedSpeciesName,
                                polygonWKT     = polygonWKT,
                                pointWKT       = pointWKT
                            )

                            val nearby = result.filter { (_, distKm) -> distKm <= radiusKm }



                            //AI rank the nearby spots
                            val aiRated = nearby.map { (plass, _) ->
                                async {
                                    val lat = plass.p.coordinates[1]
                                    val lon = plass.p.coordinates[0]

                                    val weather = dashboardViewModel.getWeatherFor(
                                        lat        = lat,
                                        lon        = lon,
                                        pointOfTime  = selectedDateTime!!,
                                        repository = dashboardViewModel.repository
                                    )

                                    if (weather != null) {
                                        val details = weather.data.instant.details
                                        val trainingData = TrainingData(
                                            speciesId     = SpeciesMapper.getId(selectedSpeciesName),
                                            temperature   = details.air_temperature.toFloat(),
                                            windSpeed     = details.wind_speed.toFloat(),
                                            precipitation = when {
                                                weather.data.next_1_hours?.details?.precipitation_amount != null ->
                                                    weather.data.next_1_hours.details.precipitation_amount.toFloat()
                                                weather.data.next_6_hours?.details?.precipitation_amount != null ->
                                                    weather.data.next_6_hours.details.precipitation_amount.toFloat()
                                                else -> 0f
                                            },
                                            airPressure   = details.air_pressure_at_sea_level.toFloat(),
                                            cloudCover    = details.cloud_area_fraction.toFloat(),
                                            timeOfDay     = selectedDateTime!!.hour.toFloat(),
                                            season        = selectedDateTime!!.monthValue / 3f,
                                            latitude      = lat.toFloat(),
                                            longitude     = lon.toFloat()
                                        )

                                        val predictor = FishPredictor(context)
                                        val input = floatArrayOf(
                                            trainingData.speciesId,
                                            trainingData.temperature,
                                            trainingData.windSpeed,
                                            trainingData.precipitation,
                                            trainingData.airPressure,
                                            trainingData.cloudCover,
                                            trainingData.timeOfDay,
                                            trainingData.season,
                                            trainingData.latitude,
                                            trainingData.longitude
                                        )

                                        val scores = predictor.predictScores(input)
                                        val probability = scores[2] + scores[3]   // good + very good
                                        plass to probability
                                    } else null
                                }
                            }.awaitAll()
                                .filterNotNull()
                                .sortedByDescending { it.second } // highest score on top

                            /* -- return both lists as a Pair -- */
                            nearby to aiRated
                        }

                        distanceSorted = sortedByDistance
                        distanceMap    = distanceSorted.toMap()
                        aiSorted = sortedByAI
                        isLoading = false
                    }
                }
            }
        }
    }

    if (selectedSpeciesId != null && selectedLocation != null && selectedDateTime != null) {
        if (isLoading) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    TopFishingSpots(
                        "Nærmeste plasser",
                        distanceSorted.take(3).map { it.first to it.second },
                        shouldShowEmptyState = true
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    TopFishingSpots(
                        title = "Beste fiskeforhold",
                        spots               = aiSorted.take(3).map { it.first to it.second.toDouble() },
                        shouldShowEmptyState = true,
                        distanceMap         = distanceMap
                    )
                }
            }
        }
    }
}

@Composable
fun PlannerInputBox(
    modifier: Modifier = Modifier,
    iconRes: Int,
    label: String,
    onClick: () -> Unit
) {
    Card(
        shape  = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = modifier
            .padding(4.dp)
            .clickable { onClick() },

        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                modifier = Modifier
                    .size(40.dp)
                    .padding(bottom = 6.dp)
            )
            Text(
                text      = label,
                fontSize  = 12.sp,
                textAlign = TextAlign.Center,
                maxLines  = 1,
                overflow  = TextOverflow.Ellipsis,
                fontWeight  = FontWeight.SemiBold,
                softWrap  = false
            )
        }
    }
}

@Composable
fun TopFishingSpots(
    title: String,
    spots: List<Pair<MittFiskeLocation, Double>>,
    shouldShowEmptyState: Boolean,
    distanceMap: Map<MittFiskeLocation, Double>? = null,
    isLoading: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 8.dp),
            textAlign = TextAlign.Center
        )

        if (isLoading) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
            return
        }

        if (spots.isEmpty() && shouldShowEmptyState) {
            Text(
                text = "Ingen fiskeplasser funnet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(8.dp)
            )
        } else {
            spots.forEachIndexed { index, (plass, verdi) ->

                val backgroundColor = if (title.contains("Beste", ignoreCase = true)) {
                    val base = MaterialTheme.colorScheme.primary
                    when (index) {
                        0 -> base.copy(alpha = 0.45f) // most intense color
                        1 -> base.copy(alpha = 0.30f)
                        2 -> base.copy(alpha = 0.15f)
                        else -> base.copy(alpha = 0.10f)
                    }
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }

                val distKm = distanceMap?.get(plass) ?: verdi

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = backgroundColor)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {

                        Text(
                            text = "${index + 1}. ${plass.name}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = "${"%.1f".format(distKm)} km unna",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ShowRadiusDialog(
    current: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var tmp by remember { mutableIntStateOf(current) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Text("OK",
                modifier = Modifier.clickable { onConfirm(tmp) }.padding(8.dp),
                color = MaterialTheme.colorScheme.primary)
        },
        dismissButton = {
            Text("Avbryt",
                modifier = Modifier.clickable(onClick = onDismiss).padding(8.dp))
        },
        title = { Text("Hvor langt er du villig til å reise?") },
        text  = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$tmp km", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("–", fontSize = 32.sp,
                        modifier = Modifier
                            .clickable { if (tmp > 10)  tmp -= 10 }
                            .padding(8.dp))
                    Text("+", fontSize = 32.sp,
                        modifier = Modifier
                            .clickable { if (tmp < 500) tmp += 10 }
                            .padding(8.dp))
                }
            }
        }
    )
}