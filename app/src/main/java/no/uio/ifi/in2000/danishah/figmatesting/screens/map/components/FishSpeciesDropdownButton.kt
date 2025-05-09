package no.uio.ifi.in2000.danishah.figmatesting.screens.map.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.danishah.figmatesting.R

@Composable
fun FishSpeciesDropdownButton(
    speciesOptions: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf("Art") }

    Box {
        SmallFloatingActionButton(
            onClick = { expanded = true },
            shape = CircleShape,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.fish),
                contentDescription = "Velg art"
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            speciesOptions.forEach { species ->
                DropdownMenuItem(
                    text = { Text(species) },
                    onClick = {
                        selected = species
                        expanded = false
                        onSelected(species)
                    }
                )
            }
        }
    }
}