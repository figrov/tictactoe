package com.example.game.ui

import androidx.compose.animation.core.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.game.domain.CellState
import com.example.game.domain.Player

/**
 * Отдельная клетка игрового поля
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Cell(
    index: Int,
    cellState: CellState,
    isWinningCell: Boolean = false,
    isCellAnimating: Boolean = false,
    onCellClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pulseAnimation by animateFloatAsState(
        targetValue = if (isCellAnimating) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "CellPulse"
    )

    val contentColor = when (cellState) {
        CellState.X -> MaterialTheme.colorScheme.primary
        CellState.O -> MaterialTheme.colorScheme.secondary
        else -> Color.Transparent
    }

    val scale by animateFloatAsState(
        targetValue = pulseAnimation,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "CellScale"
    )

    Box(
        modifier = modifier
            .size(100.dp)
            .background(
                if (isWinningCell)
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        when (cellState) {
            CellState.X -> {
                XIcon(
                    size = 80.dp,
                    player = Player.X,
                    modifier = Modifier
                )
            }
            CellState.O -> {
                OIcon(
                    size = 80.dp,
                    player = Player.O,
                    modifier = Modifier
                )
            }
            CellState.EMPTY -> {
                Box(modifier = Modifier.size(100.dp)) // Пустое пространство для клика
            }
        }
    }
}
