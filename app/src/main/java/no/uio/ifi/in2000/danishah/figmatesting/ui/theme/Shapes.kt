package no.uio.ifi.in2000.danishah.figmatesting.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Shape definitions for FiskeFinner app
val Shapes = Shapes(
    // Used for small components like chips, small buttons
    small = RoundedCornerShape(4.dp),
    
    // Used for medium components like cards
    medium = RoundedCornerShape(12.dp),
    
    // Used for large components like bottom sheets
    large = RoundedCornerShape(16.dp)
)

// Additional shapes
val BottomNavShape = RoundedCornerShape(
    topStart = 0.dp,
    topEnd = 0.dp,
    bottomStart = 0.dp,
    bottomEnd = 0.dp
)

val MapButtonShape = RoundedCornerShape(50) // Circular buttons

val SearchBarShape = RoundedCornerShape(28.dp)

val CardShape = RoundedCornerShape(16.dp)

val ProfilePictureShape = RoundedCornerShape(8.dp) 