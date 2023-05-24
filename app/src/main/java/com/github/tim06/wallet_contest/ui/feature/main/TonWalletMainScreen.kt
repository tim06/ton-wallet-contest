package com.github.tim06.wallet_contest.ui.feature.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.github.tim06.wallet_contest.ton.TonWalletClient
import com.github.tim06.wallet_contest.ui.components.camera.TonWalletCameraScreen
import com.github.tim06.wallet_contest.ui.feature.lock.LockScreen
import com.github.tim06.wallet_contest.ui.feature.main.content.TonWalletMainScreenContent
import com.github.tim06.wallet_contest.ui.feature.main.history.details.TonTransactionDetailsScreen
import com.github.tim06.wallet_contest.ui.feature.receive.TonReceiveScreen
import com.github.tim06.wallet_contest.ui.feature.send.TonSendRecipientModel
import com.github.tim06.wallet_contest.ui.navigation.LOCK_DESTINATION
import com.github.tim06.wallet_contest.ui.navigation.QR_SCAN_DESTINATION
import com.github.tim06.wallet_contest.ui.navigation.SEND_ADDRESS_DESTINATION
import com.github.tim06.wallet_contest.ui.navigation.SEND_CONFIRM_DESTINATION
import com.github.tim06.wallet_contest.ui.navigation.SEND_PENDING_DESTINATION
import com.github.tim06.wallet_contest.ui.navigation.sendGraph
import com.github.tim06.wallet_contest.util.toTonLong
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
@Composable
fun TonWalletMainScreen(
    tonWalletClient: TonWalletClient,
    deeplinkModel: DeeplinkModel?,
    onScanClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSendToAddressClick: () -> Unit,
    onBack: () -> Unit,
    viewModel: TonWalletMainScreenViewModel = viewModel(
        factory = TonWalletMainScreenViewModelFactory()
    )
) {
    val coroutineScope = rememberCoroutineScope()

    val navController = rememberAnimatedNavController()

    val modalSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Expanded,
        skipHalfExpanded = true
    )

    LaunchedEffect(modalSheetState.currentValue) {
        if (modalSheetState.currentValue == ModalBottomSheetValue.Hidden) {
            viewModel.onBottomSheetStateChange(false)
        }
    }

    val bottomSheetExpanded by viewModel.bottomSheetExpanded.collectAsState()

    LaunchedEffect(key1 = bottomSheetExpanded) {
        coroutineScope.launch {
            if (bottomSheetExpanded) {
                modalSheetState.show()
            } else {
                modalSheetState.hide()
            }
        }
    }

    LaunchedEffect(key1 = deeplinkModel) {
        if (deeplinkModel != null) {
            delay(300)
            val recipientModel = Json.encodeToString(TonSendRecipientModel(deeplinkModel.walletAddress))
            navController.navigate("$SEND_CONFIRM_DESTINATION?wallet_address=${recipientModel}&amount=${deeplinkModel.amount}&comment=${deeplinkModel.message}") {
                popUpTo(0)
            }
            viewModel.onBottomSheetStateChange(true)
        }
    }

    var bufferTransactionId by remember { mutableStateOf(0L) }


    ModalBottomSheetLayout(
        sheetState = modalSheetState,
        sheetShape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp),
        sheetContent = {
            AnimatedNavHost(
                navController = navController,
                startDestination = "Receive",
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None }
            ) {
                sendGraph(
                    navController = navController,
                    tonWalletClient = tonWalletClient,
                    onScanOpenClick = {
                        navController.navigate(QR_SCAN_DESTINATION)
                    },
                    onClose = {
                        viewModel.onBottomSheetStateChange(false)
                    }
                )
                composable(route = "Receive") {
                    TonReceiveScreen(client = tonWalletClient)
                }
                composable(route = "TransactionHistory") {
                    TonTransactionDetailsScreen(
                        tonWalletClient = tonWalletClient,
                        transactionId = bufferTransactionId,
                        onSendToAddressClick = { address ->
                            coroutineScope.launch {
                                navController.navigate(
                                    route = "$SEND_ADDRESS_DESTINATION?wallet_address=$address"
                                ) {
                                    popUpTo(0)
                                }
                                viewModel.onBottomSheetStateChange(true)
                            }
                        }
                    )
                }
                composable(route = QR_SCAN_DESTINATION) {
                    TonWalletCameraScreen(
                        tonWalletClient = tonWalletClient,
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onResult = { result ->
                            navController.navigate(
                                route = "$SEND_ADDRESS_DESTINATION?wallet_address=$result"
                            ) {
                                popUpTo(0)
                            }
                        }
                    )
                }
                composable(
                    route = "$LOCK_DESTINATION?destination={destination}&amount={amount}&comment={comment}",
                    arguments = listOf(
                        navArgument("destination") {
                            NavType.StringType
                        },
                        navArgument("amount") {
                            NavType.LongType
                        },
                        navArgument("comment") {
                            nullable = true
                            NavType.StringType
                        },
                    ),
                    exitTransition = { fadeOut() },
                    popExitTransition = { slideOutHorizontally(tween(500)) { it } }
                ) { entry ->
                    LockScreen(
                        tonWalletClient = tonWalletClient
                    ) {
                        navController.navigate(
                            "$SEND_PENDING_DESTINATION&destination=${
                                entry.arguments?.getString(
                                    "destination"
                                )
                            }&amount=${
                                entry.arguments?.getString("amount")
                            }&comment=${entry.arguments?.getString("comment")}"
                        )
                    }
                }
            }
        }
    ) {
        TonWalletMainScreenContent(
            tonWalletClient = tonWalletClient,
            onScanClick = onScanClick,
            onSettingsClick = onSettingsClick,
            receiveClick = {
                coroutineScope.launch {
                    navController.navigate("Receive") {
                        popUpTo(0)
                    }
                    delay(300)
                    viewModel.onBottomSheetStateChange(true)
                }
            },
            sendClick = {
                coroutineScope.launch {
                    navController.navigate(route = SEND_ADDRESS_DESTINATION) {
                        popUpTo(0)
                    }
                    delay(300)
                    viewModel.onBottomSheetStateChange(true)
                }
            },
            onTransactionClick = {
                coroutineScope.launch {
                    bufferTransactionId = it
                    navController.navigate("TransactionHistory") {
                        popUpTo(0)
                    }
                    delay(300)
                    viewModel.onBottomSheetStateChange(true)
                }
            },
            onSendToAddressClick = onSendToAddressClick
        )
    }

    BackHandler {
        if (bottomSheetExpanded) {
            viewModel.onBottomSheetStateChange(false)
        } else {
            onBack.invoke()
        }
    }
}

sealed interface BottomSheetMode {
    object Receive : BottomSheetMode
    data class Send(val walletAddress: String?) : BottomSheetMode
    data class TransactionHistory(val transactionId: Long) : BottomSheetMode
}