package com.github.tim06.wallet_contest.ui.feature.passcode

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.*
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.github.tim06.wallet_contest.ui.theme.DividerColor1
import com.github.tim06.wallet_contest.ui.theme.WhiteAlpha32
import kotlinx.coroutines.*

@Composable
fun DigitsDots(
    modifier: Modifier = Modifier,
    digitsCount: Int = 4,
    filledCount: Int,
    scaleAnimation: Boolean,
    darkMode: Boolean = false,
    onSuccessAnimationEnd: (() -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    val scale = remember { Animatable(initialValue = 1f) }
    LaunchedEffect(key1 = scaleAnimation) {
        if (scaleAnimation) {
            scope.launch {
                delay(300L)
                scale.animateTo(targetValue = 1.2f, animationSpec = tween(400))
                scale.animateTo(targetValue = 1f, animationSpec = tween(400))
                onSuccessAnimationEnd?.invoke()
            }
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(digitsCount) { index ->
            Dot(
                modifier = Modifier.scale(scale.value),
                darkMode = darkMode,
                filled = index < filledCount
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun Dot(
    modifier: Modifier = Modifier,
    darkMode: Boolean = false,
    filled: Boolean
) {
    Box(
        modifier = modifier
            .size(16.dp)
            .clip(shape = CircleShape)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 1.dp,
                    brush = SolidColor(
                        value = if (darkMode) {
                            WhiteAlpha32
                        } else {
                            DividerColor1
                        }
                    ),
                    shape = CircleShape
                )
        )
        AnimatedVisibility(
            visible = filled,
            enter = scaleIn(),
            exit = scaleOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = if (darkMode) {
                            Color.White
                        } else {
                            Color.Black
                        },
                        shape = CircleShape
                    )
            )
        }
    }
}