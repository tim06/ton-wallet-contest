package com.github.tim06.wallet_contest.ui.components

import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.github.tim06.wallet_contest.R
import com.github.tim06.wallet_contest.ui.components.animation.animateDpSizeAsState
import com.github.tim06.wallet_contest.ui.components.rlottie.LottieIcon
import com.github.tim06.wallet_contest.ui.components.topBar.TonTopAppBar
import kotlin.math.roundToInt

@Composable
fun ScrollableTitleContainer(
    @StringRes title: Int,
    description: String,
    @RawRes icon: Int,
    backClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val density = LocalDensity.current
    val topInsetHeightDp = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val topInsetHeightPx = with(density) { topInsetHeightDp.toPx() }

    val scrollState = rememberScrollState()

    var topBarTitlePosition by remember { mutableStateOf(Offset.Zero) }
    var resultTitleSize by remember { mutableStateOf(IntSize.Zero) }

    var initialTitlePositionX by remember { mutableStateOf(Float.NaN) }
    var initialTitleSize by remember { mutableStateOf(IntSize.Zero) }
    var initialTitlePositionY by remember { mutableStateOf(Float.NaN) }
    val initialTitleWithInsetY by remember(initialTitlePositionY) {
        derivedStateOf {
            initialTitlePositionY - topInsetHeightPx
        }
    }

    val scaleFactor by remember { mutableStateOf(20f / 24f) }
    val scalePadding = with (LocalDensity.current) { 4.dp.toPx() }

    val distanceInitialToResultY by remember(initialTitlePositionY, topBarTitlePosition) {
        derivedStateOf {
            initialTitlePositionY - topBarTitlePosition.y + scalePadding
        }
    }

    val offsetY by remember(scrollState, distanceInitialToResultY) {
        derivedStateOf {
            -(scrollState.value.coerceAtMost(distanceInitialToResultY.toInt()))
        }
    }

    val progress by remember(scrollState) {
        derivedStateOf {
            ((scrollState.value - 0) / (distanceInitialToResultY - 0)).coerceIn(
                0f,
                1f
            )
        }
    }

    val padding = with (LocalDensity.current) { 16.dp.toPx() }
    val titlePositionX by remember(initialTitlePositionX, topBarTitlePosition, progress) {
        derivedStateOf {
            com.github.tim06.wallet_contest.util.lerp(
                initialTitlePositionX,
                topBarTitlePosition.x - scaleFactor - padding - scalePadding,
                progress
            ).toInt()
        }
    }

    val titleTextStyleFontSize by remember(progress) {
        derivedStateOf {
            com.github.tim06.wallet_contest.util.lerp(1f, scaleFactor, progress)
        }
    }

    val titleAlpha by remember(scrollState.value) {
        derivedStateOf {
            if (scrollState.value > 0) 0f else 1f
        }
    }
    val elevation by animateDpAsState(targetValue = if (scrollState.value > 0) 1.dp else 0.dp)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column {
            TonTopAppBar(
                title = "",
                elevation = elevation,
                onTitlePositioned = { layoutCoordinates ->
                    topBarTitlePosition = layoutCoordinates.positionInRoot()
                    resultTitleSize = layoutCoordinates.size
                },
                backClick = backClick
            )
            Column(
                modifier = Modifier
                    .padding(horizontal = 40.dp)
                    .verticalScroll(state = scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LottieIcon(modifier = Modifier.size(100.dp), icon = icon, iconSize = DpSize(100.dp, 100.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        modifier = Modifier
                            .onGloballyPositioned { layoutCoordinates ->
                                if (initialTitlePositionY.isNaN()) {
                                    initialTitlePositionY = layoutCoordinates.positionInRoot().y
                                }
                                initialTitleSize = layoutCoordinates.size
                                initialTitlePositionX = layoutCoordinates.positionInRoot().x
                            }
                            .alpha(titleAlpha),
                        text = stringResource(id = title),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.body1
                    )
                    Text(
                        text = description,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.body2
                    )
                }
                content.invoke(this)
            }
        }

        Text(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = 0,
                        initialTitleWithInsetY.toInt()
                    )
                }
                .offset {
                    IntOffset(x = titlePositionX, y = offsetY)
                }
                .then(
                    // TODO investigate this bug
                    if (android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.P) {
                        Modifier
                    } else {
                        Modifier.scale(titleTextStyleFontSize)
                    }
                )
                .alpha(1 - titleAlpha),
            text = stringResource(id = title),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.body1
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun ScrollableTitlePreview() {
    ScrollableTitleContainer(
        title = R.string.create_recovery_title,
        description = stringResource(id = R.string.create_recovery_description),
        icon = R.raw.recovery_phrase,
        backClick = {}
    ) {
        Text(text = "Preview")
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
private fun measureText(@StringRes textRes: Int, style: TextStyle): IntSize {
    val textMeasurer = rememberTextMeasurer()
    val text = stringResource(id = textRes)
    return textMeasurer.measure(text, style).size
}

private val TOP_APP_BAR_HEIGHT = 56.dp
private val TOP_APP_BAR_TITLE_HEIGHT = 24.dp