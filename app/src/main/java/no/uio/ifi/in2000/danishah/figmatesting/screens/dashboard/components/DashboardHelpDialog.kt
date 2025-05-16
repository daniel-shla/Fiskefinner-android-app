package no.uio.ifi.in2000.danishah.figmatesting.screens.dashboard.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun DashboardHelpDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background
            ),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // top text and closing button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    // Midtstilt tittel
                    Text(
                        text = "Dashbord-hjelp",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    // Lukke-knapp oppe til høyre
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
                    text = "Her finner du en oversikt over dagens vær, og kan planlegge en fisketur.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    )

                Spacer(modifier = Modifier.height(16.dp))

                HelpSection(
                    icon = Icons.Default.WbSunny,
                    title = "Dagens vær",
                    iconTint = MaterialTheme.colorScheme.primary,
                    items = listOf(
                        "Her får du en oversikt over hvordan været er nå.",
                        "Hvis du ikke har tillatt å dele posisjon, vil været for Oslo vises."
                    )
                )

                HelpSection(
                    icon = Icons.Default.CalendarToday,
                    title = "Fiskeplanlegger",
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    items = listOf(
                        "Velg hvilken type fisk du vil fiske.",
                        "Velg et tidspunkt i løpet av de kommende 7 dager for når du vil fiske.",
                        "Velg på kartet hvor du ønsker å fiske.",
                        "Velg hvor langt unna valgt sted du ønsker å få anbefalte fiskeplasser."
                    )
                )

                HelpSection(
                    icon = Icons.Default.Star,
                    title = "Fiskeplass-forslag",
                    iconTint = Color(0xFFFFC107),
                    items = listOf(
                        "Når du har planlagt en fisketur får du anbefalinger om fiskeplasser som passer kriteriene dine.",
                        "Du får foreslått de nærmeste fiskeplassene, og de beste fiskeplassene innen avstanden du har valgt."
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
                Spacer(modifier = Modifier.height(16.dp))


                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Lukk")
                }
            }
        }
    }
}

// could refactor this and from MapHelpDialog to be in a separate "components"-package
// fix if we have time to spare
@Composable
fun HelpSection(
    icon: ImageVector,
    title: String,
    iconTint: Color,
    items: List<String>
) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items.forEach { item ->
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 6.dp, bottom = 6.dp)
            ) {
                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(end = 8.dp, top = 2.dp)
                )
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

    }
}