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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
fun MapHelpDialog(
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
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Karthjelp",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Lukk")
                    }
                }
                
                Text(
                    text = "Finn de beste fiskeplassene med FiskeFinner",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                HelpSection(
                    icon = Icons.Default.MyLocation,
                    title = "Navigering",
                    iconTint = MaterialTheme.colorScheme.primary,
                    items = listOf(
                        "Sveip med fingeren for å bevege kartet",
                        "Knip fingrene for å zoome inn og ut",
                        "Bruk + og - knappene for presis zooming",
                        "Trykk på lokasjonsknappen for å finne din posisjon"
                    )
                )
                
                HelpSection(
                    icon = Icons.Default.LocationOn,
                    title = "Fiskeplasser",
                    iconTint = Color(0xFF2196F3),
                    items = listOf(
                        "Blå markører viser enkeltplasser for fiske",
                        "Markører med tall viser områder med flere fiskeplasser",
                        "Trykk på markør for å se detaljer om fiskeplassen",
                        "Zoom inn for å se flere detaljer i områder med mange markører"
                    )
                )
                
                HelpSection(
                    icon = Icons.Default.Waves,
                    title = "Fiskearter og AI-modell",
                    iconTint = Color(0xFF4CAF50),
                    items = listOf(
                        "Fargede områder viser hvor det er sannsynlig å finne fiskearter",
                        "Forskjellige farger representerer ulike fiskearter",
                        "Mørkere farge betyr høyere sannsynlighet for å finne fisken",
                        "Fra FiskeArter-fanen kan du velge hvilke arter du vil se"
                    )
                )
                
                HelpSection(
                    icon = Icons.Default.Star,
                    title = "Vurderinger",
                    iconTint = Color(0xFFFFC107),
                    items = listOf(
                        "Stjerner viser AI-modellens vurdering av fiskeplassen",
                        "Vurderingen kombinerer værforhold, årstid, tid på døgnet og fiskepreferanser",
                        "Høyere antall stjerner betyr bedre fiskeforhold"
                    )
                )
                
                HelpSection(
                    icon = Icons.Default.Search,
                    title = "Søk",
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    items = listOf(
                        "Bruk søkefeltet øverst for å finne steder",
                        "Skriv inn stedsnavnet og trykk søk",
                        "Velg et forslag fra listen som dukker opp"
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "God fisketur! :)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Bottom close button
                androidx.compose.material3.Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Lukk")
                    }
                }
            }
        }
    }
}

@Composable
private fun HelpSection(
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
        
        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
} 