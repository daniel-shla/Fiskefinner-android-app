package no.uio.ifi.in2000.danishah.figmatesting.screens.map.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.FishSpeciesData

@Composable
fun FishSpeciesDialog(
    isLoading: Boolean,
    availableSpecies: List<FishSpeciesData>,
    onSelectSpecies: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Velg fiskeart",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Close button
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Lukk"
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (isLoading) {
                    // Show loading indicator
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(16.dp)
                    )
                } else {
                    LazyColumn {
                        items(availableSpecies) { species ->
                            FishSpeciesItem(
                                species = species,
                                onClick = { onSelectSpecies(species.scientificName) }
                            )
                            
                            Divider()
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun FishSpeciesItem(
    species: FishSpeciesData,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = species.commonName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = species.scientificName.replace("_", " "),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Light,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
} 