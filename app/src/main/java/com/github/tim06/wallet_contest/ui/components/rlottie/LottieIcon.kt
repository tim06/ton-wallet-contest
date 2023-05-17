package com.github.tim06.wallet_contest.ui.components.rlottie

import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.viewinterop.AndroidView
import org.telegram.RLottieImageView

@Composable
fun LottieIcon(
    modifier: Modifier = Modifier,
    @RawRes icon: Int,
    iconSize: DpSize,
    repeatable: Boolean = false
) {
    val density = LocalDensity.current
    val width = with (density) { iconSize.width.roundToPx() }
    val height = with (density) { iconSize.height.roundToPx() }
    AndroidView(
        modifier = modifier,
        factory = { context ->
            RLottieImageView(context).apply {
                setAnimation(icon, width, height)
                setAutoRepeat(repeatable)
                playAnimation()
            }
        },
        onReset = { lottieImageView ->
            lottieImageView.onAttachedToWindow()
        },
        onRelease = { lottieImageView ->
            lottieImageView.onDetachedFromWindow()
        }
    )
}