package com.example.notesbysubject.ui.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import java.util.Locale
import kotlin.math.abs

@Composable
fun LevelIndicator(
    angleDegrees: Float,
    modifier: Modifier = Modifier,
    isLevel: Boolean = isWithinTolerance(angleDegrees)
) {
    val levelColor = if (isLevel) Color(0xFF4CAF50) else Color(0xFFFF9800)
    val formattedAngle = String.format(Locale.US, "%.1f°", abs(angleDegrees))

    Column(
        modifier = modifier
            .background(
                color = Color.Black.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(width = 140.dp, height = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            // Fixed center reference dot
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(Color.White.copy(alpha = 0.6f), CircleShape)
            )

            // Tilting bar that rotates with phone tilt
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(4.dp)
                    .graphicsLayer {
                        rotationZ = -angleDegrees
                    }
                    .background(levelColor, shape = RoundedCornerShape(2.dp))
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = formattedAngle,
            style = MaterialTheme.typography.titleMedium,
            color = levelColor
        )
    }
}
