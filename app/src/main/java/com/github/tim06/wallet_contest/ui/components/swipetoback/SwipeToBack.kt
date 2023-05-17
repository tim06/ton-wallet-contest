package com.github.tim06.wallet_contest.ui.components.swipetoback

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeToBack(
    onSwipeBack: () -> Unit,
    content: @Composable () -> Unit
) {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    val screenWidthFloat = with(LocalDensity.current) { screenWidthDp.toPx() }
    var isSwiped by remember { mutableStateOf(false) }

    val swipeableState = rememberSwipeableState(initialValue = 0)

    val offsetX: Float by animateFloatAsState(swipeableState.offset.value)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .swipeable(
                state = swipeableState,
                anchors = mapOf(0f to 0, screenWidthFloat to 1),
                thresholds = { _, _ -> FractionalThreshold(0.3f) },
                orientation = Orientation.Horizontal
            )
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    translationX = offsetX
                }
                .fillMaxSize()
        ) {
            content()
        }

        if (swipeableState.isAnimationRunning.not() && offsetX == screenWidthFloat && isSwiped.not()) {
            isSwiped = true
            // Анимация возврата завершена
            onSwipeBack()
        }
    }
}




