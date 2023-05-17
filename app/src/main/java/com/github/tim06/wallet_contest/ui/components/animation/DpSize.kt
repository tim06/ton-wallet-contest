package com.github.tim06.wallet_contest.ui.components.animation

import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize

@Composable
fun animateDpSizeAsState(
    targetValue: DpSize,
    finishedListener: ((DpSize) -> Unit)? = null
): State<DpSize> {
    return animateValueAsState(
        targetValue,
        DpSize.VectorConverter,
        finishedListener = finishedListener
    )
}

@Composable
inline fun <S> Transition<S>.animateDpSize(
    noinline transitionSpec: @Composable Transition.Segment<S>.() -> FiniteAnimationSpec<DpSize> = {
        tween(300)
    },
    label: String = "DpSizeAnimation",
    targetValueByState: @Composable (state: S) -> DpSize
): State<DpSize> =
    animateValue(DpSize.VectorConverter, transitionSpec, label, targetValueByState)

val DpSize.Companion.VectorConverter: TwoWayConverter<DpSize, AnimationVector2D>
    get() = DpSizeToVector


private val DpSizeToVector: TwoWayConverter<DpSize, AnimationVector2D> = TwoWayConverter(
    convertToVector = { AnimationVector2D(it.width.value, it.height.value) },
    convertFromVector = { DpSize(Dp(it.v1), Dp(it.v2)) }
)