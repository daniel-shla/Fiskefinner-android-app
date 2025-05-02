package no.uio.ifi.in2000.danishah.figmatesting.screens.fishselection

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import no.uio.ifi.in2000.danishah.figmatesting.screens.map.FishSpeciesViewModel
import no.uio.ifi.in2000.danishah.figmatesting.screens.map.components.getColorForSpecies


@Composable
fun FishSelectionScreen(
    viewModel: FishSelectionViewModel = viewModel(factory = FishSelectionViewModel.Factory),
    fishSpeciesViewModel: FishSpeciesViewModel,
    onNavigateToMap: () -> Unit = {}
) {
    // Using the passed FishSpeciesViewModel instance
    val availableSpecies by fishSpeciesViewModel.availableSpecies.collectAsState()
    val speciesStates by fishSpeciesViewModel.speciesStates.collectAsState()
    val isLoadingSpecies by fishSpeciesViewModel.isLoading.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Fisketyper",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Fish Species Selection content
        Column {
            Text(
                text = "Velg arter som skal vises på kartet",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Add Go to Map button
            val enabledSpeciesCount = speciesStates.values.count { it.isEnabled && it.isLoaded }
            
            if (enabledSpeciesCount > 0) {
                ElevatedButton(
                    onClick = { onNavigateToMap() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gå til kartet for å se $enabledSpeciesCount arter")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Display loading indicator if loading
            if (isLoadingSpecies) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = "Laster fiskedata...",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            
            // Info text about limitations
            Text(
                text = "Du kan vise opptil 8 fiskearter samtidig på kartet.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Species list
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = speciesStates.entries.toList(),
                    key = { it.key }
                ) { (scientificName, state) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.alpha(1.0f)
                            ) {
                                // Fish species color indicator
                                if (state.isEnabled) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(getColorForSpecies(scientificName))
                                            .alpha(state.opacity)
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
                                        text = state.species.commonName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    Text(
                                        text = scientificName.replace("_", " "),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Light
                                    )
                                    
                                    if (state.isEnabled && state.isLoaded) {
                                        Text(
                                            text = "${state.species.polygons.size} polygoner",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                                
                                // Loading indicator or toggle
                                if (isLoadingSpecies && !state.isLoaded && state.isEnabled) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Switch(
                                        checked = state.isEnabled,
                                        onCheckedChange = { fishSpeciesViewModel.toggleSpecies(scientificName) }
                                    )
                                }
                            }
                            
                            // Only show opacity slider if species is enabled
                            if (state.isEnabled && state.isLoaded) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(start = 24.dp)
                                ) {
                                    Icon(
                                        imageVector = if (state.opacity < 0.1f) 
                                            Icons.Default.VisibilityOff 
                                        else 
                                            Icons.Default.Visibility,
                                        contentDescription = "Synlighet",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    
                                    Slider(
                                        value = state.opacity,
                                        onValueChange = { 
                                            fishSpeciesViewModel.updateSpeciesOpacity(scientificName, it) 
                                        },
                                        valueRange = 0f..1f,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 8.dp)
                                    )
                                    
                                    // Display opacity percentage
                                    Text(
                                        text = "${(state.opacity * 100).toInt()}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.width(32.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Add extra space at the bottom
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

