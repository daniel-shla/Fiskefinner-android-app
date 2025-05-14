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
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.MittFiskeLocation

@Composable
fun LocationInfoCard(
    location: MittFiskeLocation,
    modifier: Modifier = Modifier,
    onClose: () -> Unit
) {
    Card(
        modifier = modifier
            .wrapContentWidth()
            .widthIn(min = 160.dp, max = 320.dp)      // kortet vokser aldri over 320 dp
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            /* ---------- Tittel + X ---------- */
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(Modifier.size(48.dp))           // «dødvekt» = like bredt som ikonknappen

                Text(
                    text      = location.name,
                    style     = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    maxLines  = 2,
                    modifier  = Modifier.weight(1f)    // sørger for perfekt sentrering
                )

                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Lukk")
                }
            }

            /* ---------- Rating ---------- */
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text       = "⭐ ${location.rating ?: "?"}",
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text  = "AI-vurdering",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            /* ---------- Beskrivelse (hvis finnes) ---------- */
            location.locs.firstOrNull()?.de?.takeIf { it.isNotBlank() }?.let { desc ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text      = desc,
                    style     = MaterialTheme.typography.bodySmall,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            /* ---------- Features som ren tekst ---------- */
            location.locs.firstOrNull()?.fe?.takeIf { it.isNotEmpty() }?.let { feats ->
                Spacer(Modifier.height(4.dp))
                Text(
                    text      = feats.joinToString(" • "),
                    style     = MaterialTheme.typography.labelSmall,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
