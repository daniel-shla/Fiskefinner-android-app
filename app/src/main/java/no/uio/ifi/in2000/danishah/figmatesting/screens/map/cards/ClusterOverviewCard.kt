package no.uio.ifi.in2000.danishah.figmatesting.screens.map.cards

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
            .wrapContentWidth()
            .widthIn(min = 200.dp, max = 340.dp)   // no wider than 340 dp
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(Modifier.size(48.dp))

                Text(
                    text      = "${cluster.spots.size} fiskeplasser i dette området",
                    style     = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    maxLines  = 2,
                    modifier  = Modifier.weight(1f)
                )

                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Lukk")
                }
            }


            Spacer(Modifier.height(8.dp))

            cluster.spots.take(3).forEach { spot ->
                val loc = spot.locs.firstOrNull()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Text(
                            text       = spot.name,
                            style      = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )

                        loc?.de?.let {
                            Text(
                                text      = it,
                                style     = MaterialTheme.typography.bodySmall,
                                color     = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        loc?.fe?.takeIf { it.isNotEmpty() }?.let { feats ->
                            Text(
                                text      = feats.joinToString(" • "),
                                style     = MaterialTheme.typography.labelSmall,
                                color     = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = "⭐ ${spot.rating ?: "?"}",
                        modifier = Modifier.align(Alignment.Top)
                    )
                }
            }

            if (cluster.spots.size > 3) {
                Text(
                    "…og ${cluster.spots.size - 3} til (zoom inn for mer)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            cluster.averageRating?.let {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Gjennomsnittlig KI-vurdering: ${"%.1f".format(it)} / 4",
                    style      = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}


