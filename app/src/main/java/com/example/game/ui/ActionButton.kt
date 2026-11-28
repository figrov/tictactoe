package com.example.game.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp

/**
 * Кнопка действия с анимацией
 * @param text Текст кнопки
 * @param onClick Обработчик клика
 * @param enabled Доступность кнопки
 */
@Composable
fun ActionButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean
) {
    val scale = remember { Animatable(if (enabled) 1f else 0.8f) }

    LaunchedEffect(enabled) {
        scale.animateTo(
            targetValue = if (enabled) 1f else 0.8f,
            animationSpec = tween(durationMillis = 100)
        )
    }

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale.value),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(text = text)
    }
}
