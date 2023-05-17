package com.github.tim06.wallet_contest.ui.feature.main.header

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.*
import com.github.tim06.wallet_contest.R
import com.github.tim06.wallet_contest.storage.PrimaryCurrency
import com.github.tim06.wallet_contest.storage.getCurrencySymbol
import com.github.tim06.wallet_contest.ton.TonWalletClient
import com.github.tim06.wallet_contest.ui.components.swipeToRefresh.SwipeToRefreshState
import com.github.tim06.wallet_contest.ui.components.topBar.TonMainTopAppBar
import com.github.tim06.wallet_contest.ui.theme.BalanceInUsdTextStyle
import com.github.tim06.wallet_contest.util.calculateRadius
import com.github.tim06.wallet_contest.util.mapTo
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TonWalletMainHeader(
    modifier: Modifier = Modifier,
    tonWalletClient: TonWalletClient,
    swipeableState: SwipeableState<Int>,
    swipeToRefreshState: SwipeToRefreshState,
    successSwipe: Boolean,
    updateProgress: Int,
    address: String,
    balance: String? = null,
    onScanClick: () -> Unit,
    onSettingsClick: () -> Unit,
    receiveClick: () -> Unit,
    sendClick: () -> Unit
) {
    val alpha = when (swipeableState.progress.to) {
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

    Box(modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            TonMainTopAppBar(
                modifier = Modifier,
                status = if (updateProgress == 100) "" else stringResource(id = R.string.wallet_main_status_updating),
                titleAlpha = alpha,
                onScanClick = onScanClick,
                onSettingsClick = onSettingsClick
            )
            TonWalletMainHeaderSwipeToRefresh(
                modifier = Modifier,
                swipeToRefreshState = swipeToRefreshState,
                successSwipe = successSwipe
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                TonMainHeaderContent(
                    swipeableState = swipeableState,
                    tonWalletClient = tonWalletClient,
                    address = address,
                    balance = balance
                )
            }
        }
        WalletMainDoubleHorizontalButtons(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp)
                .offset { IntOffset(0,
                    swipeToRefreshState.position
                        .coerceAtMost(72.dp.toPx())
                        .roundToInt()
                )
                }
                .alpha(alpha),
            receiveClick = receiveClick,
            sendClick = sendClick
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun TonMainHeaderContent(
    modifier: Modifier = Modifier,
    tonWalletClient: TonWalletClient,
    swipeableState: SwipeableState<Int>,
    address: String,
    balance: String? = null
) {
    val density = LocalDensity.current
    val topInsetHeightDp = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val topInsetHeightPx = with(LocalDensity.current) { topInsetHeightDp.toPx() }
    val screenHeightDp = LocalConfiguration.current.screenHeightDp.dp
    val topAppBarHeight = with(LocalDensity.current) { TOP_APP_BAR_HEIGHT.toPx() }

    var balanceSize by remember { mutableStateOf(IntSize.Zero) }
    var initialBalancePosition by remember { mutableStateOf(Offset.Zero) }
    val expandedOffsetY by remember(initialBalancePosition, balanceSize) {
        derivedStateOf {
            val y = initialBalancePosition.y
            val screenHeightPx = with(density) { screenHeightDp.toPx() }
            val other = screenHeightPx - y
            val fromTitleToUp = screenHeightPx - other

            val balanceHeight = balanceSize.height
            val topPadding = (topAppBarHeight - balanceHeight) / 2

            -fromTitleToUp + topPadding + topInsetHeightPx
        }
    }
    val expandedOffsetX by remember(initialBalancePosition, balanceSize) {
        derivedStateOf {
            val padding = with(density) { 16.dp.toPx() }
            -(initialBalancePosition.x - padding)
        }
    }

    var offset by remember { mutableStateOf(IntOffset.Zero) }
    var alpha by remember { mutableStateOf(1f) }

    when (swipeableState.progress.to) {
        1 -> {
            alpha = 1 - swipeableState.progress.fraction
            offset = IntOffset(
                x = com.github.tim06.wallet_contest.util.lerp(
                    0f,
                    expandedOffsetX,
                    swipeableState.progress.fraction
                ).roundToInt(),
                y = com.github.tim06.wallet_contest.util.lerp(
                    0f,
                    expandedOffsetY,
                    swipeableState.progress.fraction
                ).roundToInt()
            )
        }
        0 -> {
            if (swipeableState.progress.from == 1) {
                alpha = swipeableState.progress.fraction
                offset = IntOffset(
                    x = com.github.tim06.wallet_contest.util.lerp(
                        expandedOffsetX,
                        0f,
                        swipeableState.progress.fraction
                    ).roundToInt(),
                    y = com.github.tim06.wallet_contest.util.lerp(
                        expandedOffsetY,
                        0f,
                        swipeableState.progress.fraction
                    ).roundToInt()
                )
            } else {
                alpha = 1f
                offset = IntOffset(0, 0)
            }
        }
        else -> {
            alpha = 1f
            offset = IntOffset(0, 0)
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        TonWalletMainHeaderAddress(address = address, swipeableState = swipeableState)
        Column(
            modifier = Modifier
                .onGloballyPositioned { layoutCoordinates ->
                    initialBalancePosition = layoutCoordinates.positionInRoot()
                    balanceSize = layoutCoordinates.size
                }
                .offset { offset },
        ) {
            TonWalletMainHeaderBalance(swipeableState = swipeableState, balance = balance)
            val currency by tonWalletClient.currentCurrency.collectAsState()
            if (currency != null && balance != null) {
                Text(
                    modifier = Modifier.alpha(1 - alpha),
                    text = "≈ ${tonWalletClient.currentPrimaryCurrency.getCurrencySymbol()}${(balance.toBigDecimal() * currency!!).setScale(2, RoundingMode.DOWN)}",
                    style = BalanceInUsdTextStyle
                )
            }
        }
    }
}

private val TOP_APP_BAR_HEIGHT = 56.dp