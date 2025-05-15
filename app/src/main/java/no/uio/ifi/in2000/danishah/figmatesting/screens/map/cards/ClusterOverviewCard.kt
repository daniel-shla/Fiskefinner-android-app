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

            /* ---------- Title + X ---------- */
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(Modifier.size(48.dp)) // "dead weight" = width of the icon button

                Text(
                    text      = "${cluster.spots.size} fiskeplasser i dette området",
                    style     = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    maxLines  = 2,
                    modifier  = Modifier.weight(1f) // push the text to center
                )

                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Lukk")
                }
            }


            Spacer(Modifier.height(8.dp))

            /* ---------- Up to three fishing spots ---------- */
            cluster.spots.take(3).forEach { spot ->
                val loc = spot.locs.firstOrNull()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.Top          // top anchor
                ) {
                    /* -------- Left column -------- */
                    Column(
                        modifier = Modifier
                            .weight(1f)                       // take up the whole width
                            .padding(end = 8.dp)              // some space towards rating
                    ) {
                        /* name */
                        Text(
                            text       = spot.name,
                            style      = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )

                        /* description (can use multiple lines) */
                        loc?.de?.let {
                            Text(
                                text      = it,
                                style     = MaterialTheme.typography.bodySmall,
                                color     = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        /* Features (shows all - breaks automatically) */
                        loc?.fe?.takeIf { it.isNotEmpty() }?.let { feats ->
                            Text(
                                text      = feats.joinToString(" • "),
                                style     = MaterialTheme.typography.labelSmall,
                                color     = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    /* -------- right column (rating) -------- */
                    Text(
                        text = "⭐ ${spot.rating ?: "?"}",
                        modifier = Modifier.align(Alignment.Top) // optically top-adjusted
                    )
                }
            }

            /* ---------- «…and X more» ---------- */
            if (cluster.spots.size > 3) {
                Text(
                    "…og ${cluster.spots.size - 3} til (zoom inn for mer)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            /* ---------- average rating ---------- */
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


