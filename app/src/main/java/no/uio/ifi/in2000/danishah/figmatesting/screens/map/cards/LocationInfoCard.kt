package no.uio.ifi.in2000.danishah.figmatesting.screens.map.cards

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.MittFiskeLocation

@Composable
fun LocationInfoCard(
    location: MittFiskeLocation,
    modifier: Modifier = Modifier,
    onClose: () -> Unit
) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp)) {
        Box {
            Column(modifier = Modifier.padding(12.dp)) {
                Spacer(modifier = Modifier.height(24.dp))
                Text("Fiskeplass: ${location.name}", style = MaterialTheme.typography.titleMedium)
                Text("Rating: ${location.rating ?: "ukjent"}")
                Spacer(Modifier.height(8.dp))
                Text("Antall registrerte posisjoner: ${location.locs.size}")
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
