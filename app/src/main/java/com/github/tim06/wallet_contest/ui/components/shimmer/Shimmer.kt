package com.github.tim06.wallet_contest.ui.components.shimmer

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.tim06.wallet_contest.ui.theme.ChipBackgroundColor
import com.github.tim06.wallet_contest.ui.theme.HintColor

@Composable
fun ShimmerAnimation(
    modifier: Modifier = Modifier,
    baseColor: Color = HintColor,
    highlightColor: Color = ChipBackgroundColor,
    durationMillis: Int = 500,
    delayMillis: Int = 0
) {
    val transition = rememberInfiniteTransition()

    val baseColorAnim by transition.animateColor(
        initialValue = baseColor,
        targetValue = highlightColor,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, delayMillis = delayMillis),
            repeatMode = RepeatMode.Reverse
        )
    )

    val highlightColorAnim by transition.animateColor(
        initialValue = highlightColor,
        targetValue = baseColor,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, delayMillis = delayMillis),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(modifier = modifier) {
        // Базовый цвет
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(baseColorAnim)
        )
        // Цвет подсветки
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        listOf(highlightColorAnim.copy(alpha = 0.4f), highlightColorAnim, highlightColorAnim.copy(alpha = 0.4f)),
                        start = Offset(0f, 0.5f),
                        end = Offset(1f, 0.5f)
                    )
                )
                .align(Alignment.CenterStart)
                .offset(
                    x = (-100).dp,
                    y = 0.dp
                )
        )
    }
}
