package no.uio.ifi.in2000.danishah.figmatesting.screens.fishselection

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Composable function for the Fish Selection screen
 */
@Composable
fun FishSelectionScreen(viewModel: FishSelectionViewModel = viewModel(factory = FishSelectionViewModel.Factory)) {
    // Collect state from ViewModel
    val fishTypes by viewModel.fishTypes.collectAsState()
    val selectedCount by viewModel.selectedCount.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Select Fish Types",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Choose fish species you're interested in tracking",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        // Selected count indicator
        if (selectedCount > 0) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = "$selectedCount fish types selected",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // List of fish types
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = fishTypes,
                key = { it.id }
            ) { fishType ->
                FishTypeCard(
                    fishType = fishType,
                    onToggle = { viewModel.toggleFishSelection(fishType.id) }
                )
            }
            
            // Add extra space at the bottom
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishTypeCard(
    fishType: FishType,
    onToggle: () -> Unit
) {
    val elevation by animateDpAsState(
        targetValue = if (fishType.isSelected) 8.dp else 2.dp,
        animationSpec = tween(durationMillis = 300)
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = CardDefaults.cardColors(
            containerColor = if (fishType.isSelected) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f) 
                else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Fish icon placeholder
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(
                        if (fishType.isSelected)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.WaterDrop,
                    contentDescription = null,
                    tint = if (fishType.isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(30.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Fish details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = fishType.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = fishType.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Habitat information
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = fishType.habitat,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Selection checkbox
            Checkbox(
                checked = fishType.isSelected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.outline
                )
            )
        }
        
        // Show an indicator when selected
        AnimatedVisibility(
            visible = fishType.isSelected,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
} 