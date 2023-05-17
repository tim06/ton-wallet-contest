package com.github.tim06.wallet_contest.ui.feature.main.header

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeableState
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.github.tim06.wallet_contest.ui.theme.MainWalletAddressTextStyle
import com.github.tim06.wallet_contest.ui.theme.RobotoMonoRegular

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TonWalletMainHeaderAddress(
    swipeableState: SwipeableState<Int>,
    address: String
) {
    var alpha by remember { mutableStateOf(1f) }
    alpha = when (swipeableState.progress.to) {
        1 -> 1 - swipeableState.progress.fraction
        0 -> {
            if (swipeableState.progress.from == 1) {
                swipeableState.progress.fraction
            } else {
                1f
            }
        }
        else -> 1f
    }

    Box {
        androidx.compose.animation.AnimatedVisibility(
            modifier = Modifier.alpha(alpha),
            visible = address.isEmpty().not(),
            enter = fadeIn(tween(500)),
            exit = fadeOut(tween(500))
        ) {
            Text(
                text = address.toAnnotatedAddress(),
                style = MainWalletAddressTextStyle
            )
        }
        Text(
            modifier = Modifier.alpha(0f),
            text = address.toAnnotatedAddress(),
            style = MainWalletAddressTextStyle
        )
    }
}

private fun String.toAnnotatedAddress(): AnnotatedString {
    return if (this.isNotEmpty() && isNotBlank()) {
        buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    fontFamily = RobotoMonoRegular,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White
                )
            ) {
                append(take(4))
            }

            withStyle(
                style = SpanStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White
                )
            ) {
                append("…")
            }

            withStyle(
                style = SpanStyle(
                    fontFamily = RobotoMonoRegular,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White
                )
            ) {
                append(takeLast(4))
            }
        }
    } else {
        buildAnnotatedString { }
    }
}