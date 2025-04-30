package no.uio.ifi.in2000.danishah.figmatesting.screens.map.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.danishah.figmatesting.screens.map.FishSpeciesViewModel

/**
 * A panel displaying toggles for all available fish species
 */
@Composable
fun SpeciesPanel(
    viewModel: FishSpeciesViewModel,
    onClose: () -> Unit
) {
    val scrollState = rememberScrollState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val speciesStates by viewModel.speciesStates.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    Card(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Velg fiskearter",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Lukk")
                }
            }
            
            // Error message
            errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Info text about limitations
            Text(
                text = "Merk: For å unngå minneproblemer kan kun 2 arter vises samtidig.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Species list
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .weight(1f, fill = false)
            ) {
                for ((scientificName, state) in speciesStates) {
                    SpeciesToggleRow(
                        name = state.species.commonName,
                        scientificName = scientificName.replace("_", " "),
                        isChecked = state.isEnabled,
                        isLoading = isLoading && !state.isLoaded && state.isEnabled,
                        onToggle = { 
                            viewModel.toggleSpecies(scientificName)
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

/**
 * A single row for a fish species with a toggle switch
 */
@Composable
private fun SpeciesToggleRow(
    name: String,
    scientificName: String,
    isChecked: Boolean,
    isLoading: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Fish species color indicator
        if (isChecked) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(getColorForSpecies(scientificName))
            )
        } else {
            Spacer(modifier = Modifier.width(16.dp))
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Species info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = scientificName,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Light
            )
        }
        
        // Loading indicator or toggle
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        } else {
            Switch(
                checked = isChecked,
                onCheckedChange = { onToggle() }
            )
        }
    }
}

/**
 * Get a consistent color for a given species
 */
fun getColorForSpecies(scientificName: String): Color {
    // Use a hash of the species name to get a consistent color
    val hue = (scientificName.hashCode() and 0xFFFFFF) % 360
    return Color.hsl(hue.toFloat(), 0.7f, 0.5f)
} 