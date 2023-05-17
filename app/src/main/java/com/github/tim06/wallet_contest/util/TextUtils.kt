package com.github.tim06.wallet_contest.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp

@OptIn(ExperimentalTextApi::class)
@Composable
fun calculateTextWidth(
    text: String = " ",
    style: TextStyle
): Dp {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    return with(density) {
        textMeasurer.measure(
            text = text,
            style = style
        ).size.width.toDp()
    }
}