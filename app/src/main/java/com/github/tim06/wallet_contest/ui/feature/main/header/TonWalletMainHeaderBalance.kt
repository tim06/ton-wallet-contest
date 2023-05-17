package com.github.tim06.wallet_contest.ui.feature.main.header

import androidx.compose.animation.*
import androidx.compose.animation.core.InternalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.github.tim06.wallet_contest.R
import com.github.tim06.wallet_contest.ui.components.rlottie.LottieIcon
import com.github.tim06.wallet_contest.ui.theme.BalanceBigTextStyle
import com.github.tim06.wallet_contest.ui.theme.RobotoMedium
import com.github.tim06.wallet_contest.ui.theme.SansMedium
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
@Composable
fun TonWalletMainHeaderBalance(
    swipeableState: SwipeableState<Int>,
    balance: String? = null,
    viewModel: TonWalletMainHeaderBalanceViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = TonWalletMainHeaderBalanceViewModelFactory()
    )
) {
    var iconSize by remember { mutableStateOf(DpSize.Zero) }
    var balanceTextStyle by remember { mutableStateOf(TextStyle()) }

    var savedBaseline by remember { mutableStateOf(0) }
    var baseline by remember { mutableStateOf(-1) }

    when (swipeableState.progress.to) {
        1 -> {
            iconSize = lerp(
                DpSize(44.dp, 56.dp),
                DpSize(18.dp, 22.dp),
                swipeableState.progress.fraction
            )
            balanceTextStyle = lerp(
                BalanceBigTextStyle,
                TextStyle(
                    fontFamily = RobotoMedium,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp,
                    lineHeight = 24.sp,
                    color = Color.White
                ),
                swipeableState.progress.fraction
            )
            baseline = com.github.tim06.wallet_contest.util.lerp(
                savedBaseline.toFloat(),
                0f,
                swipeableState.progress.fraction
            ).roundToInt()
        }
        0 -> {
            if (swipeableState.progress.from == 1) {
                iconSize = lerp(
                    DpSize(18.dp, 22.dp),
                    DpSize(44.dp, 56.dp),
                    swipeableState.progress.fraction.coerceIn(0f, 1f)
                )
                balanceTextStyle = lerp(
                    TextStyle(
                        fontFamily = RobotoMedium,
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp,
                        lineHeight = 24.sp,
                        color = Color.White
                    ),
                    BalanceBigTextStyle,
                    swipeableState.progress.fraction
                )
                baseline = com.github.tim06.wallet_contest.util.lerp(
                    0f,
                    savedBaseline.toFloat(),
                    swipeableState.progress.fraction.coerceIn(0f, 1f)
                ).roundToInt()
            } else {
                iconSize = DpSize(44.dp, 56.dp)
                balanceTextStyle = BalanceBigTextStyle
                baseline = savedBaseline
            }
        }
        else -> {
            iconSize = DpSize(44.dp, 56.dp)
            balanceTextStyle = BalanceBigTextStyle
            baseline = savedBaseline
        }
    }

    var oldBalance by remember { mutableStateOf(balance) }
    SideEffect {
        oldBalance = balance
    }

    var isContentSizeAnimationEnabled by remember { mutableStateOf(true) }

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        LottieIcon(
            modifier = Modifier.size(iconSize),
            iconSize = iconSize,
            icon = R.raw.main
        )
        if (balance != null) {
            if (viewModel.isFirstReveal) {
                Row(
                    modifier = Modifier.then(
                        if (isContentSizeAnimationEnabled) {
                            Modifier.animateContentSize(animationSpec = tween(300))
                        } else {
                            Modifier
                        }
                    )
                ) {
                    val dotIndex = remember(balance) {
                        if (balance.contains(".")) {
                            balance.indexOf(".")
                        } else {
                            Int.MAX_VALUE
                        }
                    }

                    val balanceString = remember(balance) { balance.toString() }
                    val oldBalanceString = remember(oldBalance) { oldBalance.toString() }
                    val lastIndex = remember(balanceString) { balanceString.lastIndex }

                    for (i in balanceString.indices) {
                        val isBeforeDot = i < dotIndex
                        val oldChar = oldBalanceString.getOrNull(i)
                        val newChar = balanceString[i]
                        val char1 = if (oldChar == newChar) {
                            oldBalanceString[i]
                        } else {
                            balanceString[i]
                        }

                        var delayedChar by remember { mutableStateOf<Char>(Char.MIN_VALUE) }
                        LaunchedEffect(key1 = char1) {
                            if (viewModel.isFirstReveal) {
                                delay(500L * i)
                            }
                            delayedChar = char1
                        }

                        AnimatedContent(
                            modifier = Modifier.alignBy { if (isBeforeDot) 0 else baseline },
                            targetState = delayedChar,
                            transitionSpec = {
                                when {
                                    balance == "0" -> {
                                        fadeIn(tween(700)) + scaleIn(tween(700)) with fadeOut(
                                            tween(
                                                700
                                            )
                                        ) + scaleOut(tween(500))
                                    }
                                    isBeforeDot -> {
                                        fadeIn(tween(700)) + slideIn(tween(500)) {
                                            IntOffset(
                                                it.width / 2,
                                                100
                                            )
                                        } with fadeOut(tween(700)) + scaleOut(tween(500))
                                    }
                                    else -> {
                                        fadeIn(tween(700)) with fadeOut(tween(700)) + scaleOut(
                                            tween(
                                                500
                                            )
                                        )
                                    }
                                }
                            }
                        ) { char2 ->
                            Text(
                                text = char2.toAnnotatedBalance(
                                    swipeableState = swipeableState,
                                    isBeforeDot = isBeforeDot
                                ),
                                style = balanceTextStyle,
                                onTextLayout = {
                                    if (savedBaseline == 0) {
                                        savedBaseline =
                                            -(it.size.height - it.lastBaseline.roundToInt())
                                    }
                                }
                            )
                            if (i == lastIndex && transition.currentState == transition.targetState) {
                                isContentSizeAnimationEnabled = false
                            }
                            if (i == balanceString.lastIndex && transition.segment.targetState == EnterExitState.Visible && transition.segment.initialState == EnterExitState.PreEnter) {
                                viewModel.onRevealed()
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = balance.toAnnotatedBalance(swipeableState = swipeableState),
                    style = balanceTextStyle,
                    onTextLayout = {
                        if (savedBaseline == 0) {
                            savedBaseline =
                                -(it.size.height - it.lastBaseline.roundToInt())
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
private fun Char.toAnnotatedBalance(
    swipeableState: SwipeableState<Int>,
    isBeforeDot: Boolean
): AnnotatedString {
    return buildAnnotatedString {
        withStyle(
            style = if (isBeforeDot) {
                swipeableState.getAnnotatedBalanceLeft()
            } else {
                swipeableState.getAnnotatedBalanceRight()
            }
        ) {
            append(this@toAnnotatedBalance)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
private fun String.toAnnotatedBalance(
    swipeableState: SwipeableState<Int>
): AnnotatedString {
    val left: String
    val right: String
    if (contains(".")) {
        val splited = split(".")
        left = splited.firstOrNull().orEmpty()
        right = ".${splited.lastOrNull().orEmpty()}"
    } else {
        left = ""
        right = this
    }
    return buildAnnotatedString {
        withStyle(
            style = swipeableState.getAnnotatedBalanceLeft()
        ) {
            append(left)
        }
        withStyle(
            style = swipeableState.getAnnotatedBalanceRight()
        ) {
            append(right)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
private fun SwipeableState<Int>.getAnnotatedBalanceLeft(): SpanStyle = when (progress.to) {
    1 -> {
        lerp(
            SpanStyle(
                fontFamily = SansMedium,
                fontWeight = FontWeight.Medium,
                fontSize = 44.sp,
                color = Color.White
            ),
            SpanStyle(
                fontFamily = RobotoMedium,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                color = Color.White
            ),
            progress.fraction
        )
    }
    0 -> {
        if (progress.from == 1) {
            lerp(
                SpanStyle(
                    fontFamily = RobotoMedium,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp,
                    color = Color.White
                ),
                SpanStyle(
                    fontFamily = SansMedium,
                    fontWeight = FontWeight.Medium,
                    fontSize = 44.sp,
                    color = Color.White
                ),
                progress.fraction
            )
        } else {
            SpanStyle(
                fontFamily = SansMedium,
                fontWeight = FontWeight.Medium,
                fontSize = 44.sp,
                color = Color.White
            )
        }
    }
    else -> {
        SpanStyle(
            fontFamily = SansMedium,
            fontWeight = FontWeight.Medium,
            fontSize = 44.sp,
            color = Color.White
        )
    }
}


@OptIn(ExperimentalMaterialApi::class)
private fun SwipeableState<Int>.getAnnotatedBalanceRight(): SpanStyle =
    when (progress.to) {
        1 -> {
            lerp(
                SpanStyle(
                    fontFamily = SansMedium,
                    fontWeight = FontWeight.Medium,
                    fontSize = 32.sp,
                    color = Color.White
                ),
                SpanStyle(
                    fontFamily = RobotoMedium,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp,
                    color = Color.White
                ),
                progress.fraction
            )
        }
        0 -> {
            if (progress.from == 1) {
                lerp(
                    SpanStyle(
                        fontFamily = RobotoMedium,
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp,
                        color = Color.White
                    ),
                    SpanStyle(
                        fontFamily = SansMedium,
                        fontWeight = FontWeight.Medium,
                        fontSize = 32.sp,
                        color = Color.White
                    ),
                    progress.fraction
                )
            } else {
                SpanStyle(
                    fontFamily = SansMedium,
                    fontWeight = FontWeight.Medium,
                    fontSize = 32.sp,
                    color = Color.White
                )
            }
        }
        else -> {
            SpanStyle(
                fontFamily = SansMedium,
                fontWeight = FontWeight.Medium,
                fontSize = 32.sp,
                color = Color.White
            )
        }
    }