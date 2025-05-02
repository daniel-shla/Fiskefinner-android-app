package no.uio.ifi.in2000.danishah.figmatesting.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp



@Composable
fun FiskeFinnerCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = CardShape,
        colors = CardDefaults.cardColors(
            containerColor = MediumDarkBlue
        )
    ) {
        Box(
            modifier = Modifier.padding(16.dp)
        ) {
            content()
        }
    }
}


@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = LightBlue,
            disabledContainerColor = InactiveBlue
        )
    ) {
        Text(
            text = text,
            style = Typography.labelLarge
        )
    }
}

@Composable
fun SearchBarBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(SearchBarShape)
            .background(White)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        content()
    }
}

@Composable
fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = Typography.titleMedium,
        color = White,
        modifier = modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun FiskeFinnerDivider(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(InactiveBlue)
    )
} 