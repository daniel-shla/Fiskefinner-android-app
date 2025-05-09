package no.uio.ifi.in2000.danishah.figmatesting.screens.map.cards

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.Cluster

@Composable
fun ClusterOverviewCard(
    cluster: Cluster,
    modifier: Modifier = Modifier,
    onClose: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth(0.92f)
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box {
            Column(modifier = Modifier.padding(16.dp)) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "${cluster.spots.size} fiskeplasser i dette området",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(12.dp))

                cluster.spots.take(3).forEach { spot ->
                    val loc = spot.locs.firstOrNull()
                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        Text(
                            text = spot.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        loc?.de?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⭐ ${spot.rating ?: "?"}")
                            loc?.fe?.takeIf { it.isNotEmpty() }?.let { features ->
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = features.joinToString(" • "),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                if (cluster.spots.size > 3) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "…og ${cluster.spots.size - 3} til",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                cluster.averageRating?.let {
                    Text(
                        text = "Gjennomsnittlig AI-vurdering: ${"%.1f".format(it)} / 5",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Lukk"
                )
            }
        }
    }
}
