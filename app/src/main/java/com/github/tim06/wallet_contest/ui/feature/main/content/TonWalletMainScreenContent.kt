package com.github.tim06.wallet_contest.ui.feature.main.content

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.tim06.wallet_contest.ton.TonWalletClient
import com.github.tim06.wallet_contest.ui.components.swipeToRefresh.rememberSwipeToRefreshState
import com.github.tim06.wallet_contest.ui.feature.main.bottom.TonWalletMainBottomSheet
import com.github.tim06.wallet_contest.ui.feature.main.bottom.TonWalletMainBottomSheetCreated
import com.github.tim06.wallet_contest.ui.feature.main.header.TonWalletMainHeader
import com.github.tim06.wallet_contest.ui.feature.main.history.TonWalletMainHistoryList
import com.github.tim06.wallet_contest.util.SystemBarIconsDark
import com.github.tim06.wallet_contest.util.formatCurrency
import com.github.tim06.wallet_contest.util.transformAddress
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TonWalletMainScreenContent(
    tonWalletClient: TonWalletClient,
    onScanClick: () -> Unit,
    onSettingsClick: () -> Unit,
    receiveClick: () -> Unit,
    sendClick: () -> Unit,
    onTransactionClick: (Long) -> Unit,
    onSendToAddressClick: () -> Unit,
    // TODO crash back click https://github.com/google/accompanist/issues/1487
    viewModel: TonWalletMainScreenContentViewModel = viewModel(
        factory = TonWalletMainScreenContentViewModelFactory(tonWalletClient)
    )
) {
    SystemBarIconsDark(isDark = false)
    var swipeToRefreshSuccess by remember { mutableStateOf(false) }
    val swipeableState = rememberSwipeableState(initialValue = 0)

    var isRefreshing by remember { mutableStateOf(false) }
    val swipeToRefreshState = rememberSwipeToRefreshState(
        refreshing = isRefreshing,
        refreshThreshold = 72.dp,
        refreshingOffset = 0.dp,
        onRefresh = {
            swipeToRefreshSuccess = true
            tonWalletClient.updateCurrentAccountState()
        }
    )
    LaunchedEffect(key1 = swipeToRefreshState.position) {
        if (swipeToRefreshState.position == 0f) {
            swipeToRefreshSuccess = false
        }
    }

    val screenHeightDp = LocalConfiguration.current.screenHeightDp.dp
    val headerWithoutTopAppBar = with(LocalDensity.current) { TOP_APP_BAR_HEIGHT.toPx() }
    val collapsedOffsetPx = with(LocalDensity.current) { HEADER_HEIGHT.toPx() }
    val swipeAnchors = remember {
        mapOf(
            0f to 0,
            -(collapsedOffsetPx - headerWithoutTopAppBar) to 1
        )
    }

    val scrollState = rememberLazyListState()
    val connection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val delta = available.y
                return if (delta < 0) {
                    swipeableState.performDrag(delta).toOffset()
                } else {
                    Offset.Zero
                }
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val delta = available.y
                return swipeableState.performDrag(delta).toOffset()
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                return if (available.y < 0 && scrollState.firstVisibleItemIndex == 0 && scrollState.firstVisibleItemScrollOffset == 0) {
                    swipeableState.performFling(available.y)
                    available
                } else {
                    Velocity.Zero
                }
            }

            override suspend fun onPostFling(
                consumed: Velocity,
                available: Velocity
            ): Velocity {
                swipeableState.performFling(velocity = available.y)
                return super.onPostFling(consumed, available)
            }

            private fun Float.toOffset() = Offset(0f, this)
        }
    }

    val contentHeight by remember(swipeableState) {
        derivedStateOf {
            val collapsed = screenHeightDp - HEADER_HEIGHT
            val expanded = screenHeightDp - TOP_APP_BAR_HEIGHT
            val expandedRefresh = collapsed - 72.dp

            when (swipeableState.progress.to) {
                1 -> lerp(collapsed, expanded, swipeableState.progress.fraction)
                -1 -> lerp(collapsed, expandedRefresh, swipeableState.progress.fraction)
                0 -> {
                    when (swipeableState.progress.from) {
                        1 -> lerp(expanded, collapsed, swipeableState.progress.fraction)
                        -1 -> lerp(expandedRefresh, collapsed, swipeableState.progress.fraction)
                        else -> collapsed
                    }
                }
                else -> collapsed
            }
        }
    }

    val walletData by viewModel.walletData.collectAsState(initial = null)

    val address by remember(walletData) {
        derivedStateOf {
            walletData?.address?.transformAddress().orEmpty()
        }
    }
    val balance by remember(walletData) {
        derivedStateOf {
            walletData?.balance?.formatCurrency()?.toString() ?: "0"
        }
    }
    val updatingProgress by tonWalletClient.syncProgressFlow.collectAsState(initial = 100)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colors.surface)
            .swipeable(
                state = swipeableState,
                anchors = swipeAnchors,
                thresholds = { _, _ ->
                    FractionalThreshold(0.4f)
                },
                orientation = Orientation.Vertical
            )
            .pullRefresh(
                onPull = swipeToRefreshState::onPull,
                onRelease = swipeToRefreshState::onRelease
            )
            .verticalScroll(rememberScrollState())
            .nestedScroll(connection)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        TonWalletMainHeader(
            modifier = Modifier.height(HEADER_HEIGHT),
            tonWalletClient = tonWalletClient,
            swipeableState = swipeableState,
            successSwipe = swipeToRefreshSuccess,
            updateProgress = updatingProgress,
            swipeToRefreshState = swipeToRefreshState,
            address = address,
            balance = balance,
            onScanClick = onScanClick,
            onSettingsClick = onSettingsClick,
            receiveClick = receiveClick,
            sendClick = sendClick
        )
        TonWalletMainBottomSheet(
            modifier = Modifier
                .height(contentHeight)
                .offset(y = HEADER_HEIGHT)
                .offset { IntOffset(0, swipeableState.offset.value.roundToInt()) }
                .offset {
                    IntOffset(
                        x = 0,
                        y = swipeToRefreshState.position
                            .coerceAtMost(72.dp.toPx())
                            .roundToInt()
                    )
                }
        ) {
            val transactions by viewModel.transactions.collectAsState(initial = emptyMap())
            if (transactions.isEmpty()) {
                TonWalletMainBottomSheetCreated(
                    modifier = Modifier.fillMaxWidth().align(Alignment.Center),
                    address = walletData?.address.orEmpty()
                )
            } else {
                TonWalletMainHistoryList(
                    modifier = Modifier.fillMaxSize(),
                    transactions = transactions,
                    lazyListState = scrollState,
                    onTransactionClick = onTransactionClick
                )
            }
            //TonWalletMainBottomSheetLoading(modifier = Modifier.fillMaxSize().padding(bottom = HEADER_HEIGHT))
        }
    }
}

private val TOP_APP_BAR_HEIGHT = 56.dp
private val HEADER_HEIGHT = 300.dp