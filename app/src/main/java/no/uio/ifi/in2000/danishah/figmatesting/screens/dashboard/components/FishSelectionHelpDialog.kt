package no.uio.ifi.in2000.danishah.figmatesting.screens.dashboard.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun FishSelectionHelpDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Top text and close button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = "Artvelger-hjelp",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    // Close button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(40.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Lukk")
                    }
                }
                Text(
                    text = "Her velger du hvilke fiskearter du ønsker å se på kartet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(16.dp))

                HelpSection(
                    icon = Icons.Default.Map,
                    title = "Forekomst på kartet",
                    iconTint = MaterialTheme.colorScheme.primary,
                    items = listOf(
                        "Hver art vises med egen fargede polygoner på kartet.",
                        "Områdene er markert med polygoner basert på historiske forekomster og data for fisketypene langs den norske kyst.",
                        "Du kan se opptil 8 arter samtidig."
                    )
                )

                HelpSection(
                    icon = Icons.Default.Visibility,
                    title = "Rangering og visning",
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    items = listOf(
                        "Områder med høy sannsynlighet for gode fiskeforhold er tydeligere markert på kartet.",
                        "Jo mørkere et område vises, jo bedre er det rangert for valgt art.",
                        "Rangeringen baserer seg på gitte værforhold og tar hensyn til de ulike forholdene som er gunstige for valgt art"
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "God tur og skitt fiske!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Vi tar forbehold om at anbefalingene Fiskeplanlegger gir om fiskeplasser ikke er perfekte. " +
                            "Anbefalingene er basert på værdata for lokasjonen og tidspunktet du velger, " +
                            "og vil være mer usikker jo lenger frem i tid du ønsker å fiske.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Lukk")
                }
            }
        }
    }
}
