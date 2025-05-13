package no.uio.ifi.in2000.danishah.figmatesting.screens.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import no.uio.ifi.in2000.danishah.figmatesting.R
import no.uio.ifi.in2000.danishah.figmatesting.ml.SpeciesMapper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishSpeciesSelectionScreen(navController: NavController) {
    val speciesList = listOf(
        "torsk", "makrell", "sei", "ørret", "sjøørret", "laks",
        "gjedde", "røye", "hyse", "abbor", "havabbor",
        "steinbit", "kveite", "rødspette"
    )
    val supportedInlandSpecies = listOf(
        "ørret", "røye", "gjedde", "abbor", "laks"
    )


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Hva ser du etter sjef?",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 0.dp)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Tilbake")
                    }
                }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            items(supportedInlandSpecies) { species ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val id = SpeciesMapper.getId(species).toInt()
                            navController.previousBackStackEntry?.savedStateHandle?.set(
                                "selectedSpeciesId",
                                id
                            )
                            navController.popBackStack()
                        }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(id = getImageForSpecies(species)),
                            contentDescription = species,
                            modifier = Modifier.size(100.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = species.replaceFirstChar(Char::uppercase),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

fun getImageForSpecies(species: String): Int {
    return when (species.lowercase()) {
        "abbor" -> R.drawable.abbor
        "røye" -> R.drawable.oerret
        "ørret" -> R.drawable.trout
        "laks" -> R.drawable.salmon_icon
        "gjedde" -> R.drawable.gjedde
        else -> R.drawable.fish // fallback
    }
}