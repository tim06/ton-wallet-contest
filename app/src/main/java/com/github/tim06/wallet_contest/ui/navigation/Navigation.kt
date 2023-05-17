package com.github.tim06.wallet_contest.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.github.tim06.wallet_contest.storage.Storage
import com.github.tim06.wallet_contest.ton.TonWalletClient
import com.github.tim06.wallet_contest.ui.components.camera.TonWalletCameraScreen
import com.github.tim06.wallet_contest.ui.feature.dApps.DAppsScreen
import com.github.tim06.wallet_contest.ui.feature.lock.LockScreen
import com.github.tim06.wallet_contest.ui.feature.main.DeeplinkModel
import com.github.tim06.wallet_contest.ui.feature.main.TonWalletMainScreen
import com.github.tim06.wallet_contest.ui.feature.main.history.details.TonTransactionDetailsScreen
import com.github.tim06.wallet_contest.ui.feature.receive.TonReceiveScreen
import com.github.tim06.wallet_contest.ui.feature.settings.WalletSettingsScreen
import com.github.tim06.wallet_contest.ui.feature.start.StartScreen
import com.github.tim06.wallet_contest.ui.feature.tonConnect.*
import com.github.tim06.wallet_contest.ui.feature.tonConnect.transfer.TonConnectBottomSheetTransferContent
import com.github.tim06.wallet_contest.ui.theme.BlackAlpha30
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
@Composable
fun Navigation(
    tonWalletClient: TonWalletClient,
    storage: Storage,
    deeplinkFlow: Flow<DeeplinkModel?>,
    viewModel: TonConnectViewModel = viewModel(
        factory = TonConnectViewModelViewModelFactory(
            tonWalletClient,
            tonWalletClient.tonConnectManager
        )
    )
) {
    val bottomSheetShow by viewModel.bottomSheetShow.collectAsState()
    val request by viewModel.connectionRequest.collectAsState(initial = null)
    val transfer by viewModel.transferRequest.collectAsState(initial = null)

    val modalSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = false,
        confirmValueChange = { true }
    )

    LaunchedEffect(bottomSheetShow) {
        if (bottomSheetShow is BottomSheetState.ConnectionRequest || bottomSheetShow is BottomSheetState.TransferRequest) {
            modalSheetState.show()
        }
    }

    LaunchedEffect(modalSheetState.currentValue) {
        if (modalSheetState.currentValue == ModalBottomSheetValue.Hidden) {
            viewModel.onBottomSheetDismiss()
        }
    }


    val globalNavController = rememberAnimatedNavController()
    val currentBackStackEntry1 by globalNavController.currentBackStackEntryAsState()
    val currentRoute1 by remember {
        derivedStateOf {
            currentBackStackEntry1?.destination?.route ?: "PASSCODE"
        }
    }

    var passcodeSuccessAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    LaunchedEffect(passcodeSuccessAction, currentRoute1, modalSheetState) {
        if (passcodeSuccessAction != null && currentRoute1 == "MAIN") {
            passcodeSuccessAction?.invoke()
            passcodeSuccessAction = null
        }
    }

    AnimatedNavHost(
        modifier = Modifier.fillMaxSize(),
        navController = globalNavController,
        startDestination = if (tonWalletClient.isWalletExists()) "PASSCODE" else "MAIN",
        enterTransition = { slideInHorizontally(tween(500)) { it } }
    ) {
        composable("PASSCODE") {
            LockScreen(
                tonWalletClient = tonWalletClient
            ) {
                globalNavController.navigate("MAIN") {
                    popUpTo("PASSCODE") {
                        inclusive = true
                    }
                }
            }
        }

        composable(
            route = "MAIN",
            arguments = listOf(
                navArgument("wallet_address") { nullable = true; type = NavType.StringType },
                navArgument("amount") { nullable = true; type = NavType.StringType },
                navArgument("text") { nullable = true; type = NavType.StringType },
            ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "ton://transfer/{wallet_address}?amount={amount}&text={text}"
                }
            )
        ) { backStackEntry ->
            val walletAddress = backStackEntry.arguments?.getString("wallet_address")
            val amount = backStackEntry.arguments?.getString("amount")
            val comment = backStackEntry.arguments?.getString("text")
            val navController = rememberAnimatedNavController()
            ModalBottomSheetLayout(
                sheetState = modalSheetState,
                sheetShape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp),
                sheetBackgroundColor = Color.Transparent,
                scrimColor = BlackAlpha30,
                sheetContent = {
                    val coroutineScope = rememberCoroutineScope()
                    when (bottomSheetShow) {
                        is BottomSheetState.ConnectionRequest -> {
                            if (request != null) {
                                val tonConnectButtonState by viewModel.buttonState.collectAsState()
                                TonConnectBottomSheetConnectContent(
                                    data = request!!,
                                    buttonState = tonConnectButtonState,
                                    onClose = {
                                        coroutineScope.launch {
                                            modalSheetState.hide()
                                        }
                                    },
                                    onConnectClick = {
                                        passcodeSuccessAction = {
                                            viewModel.approveConnectionRequest(request!!.data)
                                        }
                                        globalNavController.navigate("PASSCODE")
                                    }
                                )
                            }
                        }

                        is BottomSheetState.TransferRequest -> {
                            when (val transferData = transfer) {
                                is TonConnectEvent.TonConnectTransactionRequest -> {
                                    TonConnectBottomSheetTransferContent(
                                        tonWalletClient = tonWalletClient,
                                        from = transferData.from,
                                        recipient = transferData.recipient,
                                        amount = transferData.amount,
                                        onCloseClick = {
                                            coroutineScope.launch {
                                                modalSheetState.hide()
                                            }
                                        },
                                        onPasscodeRequest = {
                                            globalNavController.navigate("PASSCODE")
                                        }
                                    )
                                }

                                else -> throw UnsupportedOperationException()
                            }
                        }

                        else -> Unit
                    }
                }
            ) {
                val startDestination = remember {
                    if (tonWalletClient.isWalletExists()) {
                        WALLET_MAIN_DESTINATION
                    } else {
                        START_DESTINATION
                    }
                }

                AnimatedNavHost(
                    modifier = Modifier.fillMaxSize(),
                    navController = navController,
                    startDestination = startDestination,
                    enterTransition = { slideInHorizontally(tween(500)) { it } },
                    exitTransition = {
                        shrinkOut(
                            animationSpec = tween(
                                durationMillis = 1000,
                                delayMillis = 0,
                                easing = { 0f }
                            ),
                            shrinkTowards = Alignment.Center,
                            clip = false
                        )
                    },
                    popEnterTransition = {
                        fadeIn(tween(0))
                        /*expandIn(
                            animationSpec = tween(
                                durationMillis = 0,
                                delayMillis = 0,
                                easing = { 0f }
                            ),
                            expandFrom = Alignment.Center,
                            clip = false
                        )*/
                        /*fadeIn(
                            animationSpec = tween(
                                durationMillis = 0,
                                easing = { 0f }
                            )
                        )*/
                    },
                    popExitTransition = { slideOutHorizontally(/*tween(500)*/) { it } }
                ) {
                    composable(START_DESTINATION) {
                        StartScreen(
                            tonWalletClient = tonWalletClient,
                            onCreateClick = {
                                navController.navigate(CREATE_WALLET_CONGRATULATIONS_DESTINATION)
                            },
                            onImportClick = { navController.navigate(IMPORT_WALLET_DESTINATION) }
                        )
                    }
                    composable(SETTINGS_DESTINATION) {
                        WalletSettingsScreen(
                            walletClient = tonWalletClient,
                            onBackClick = {
                                navController.popBackStack()
                            },
                            onListOfTokensClick = {
                                navController.navigate("$LOCK_DESTINATION/successDestination=$CREATE_WALLET_RECOVERY_PHRASES_DESTINATION&standalone=true")
                            },
                            ondAppsClick = {
                                navController.navigate(D_APPS_DESTINATION)
                            },
                            onShowRecoveryPhraseClick = {
                                navController.navigate("$LOCK_DESTINATION/successDestination=$CREATE_WALLET_RECOVERY_PHRASES_DESTINATION&standalone=true")
                            },
                            onChangePasscodeClick = {

                            }
                        )
                    }
                    composable(
                        route = WALLET_MAIN_DESTINATION
                    ) {
                        val deeplinkModelFlow by deeplinkFlow.collectAsState(initial = null)
                        TonWalletMainScreen(
                            tonWalletClient = tonWalletClient,
                            deeplinkModel = if (walletAddress != null && !transition.isRunning) {
                                val model = DeeplinkModel(
                                    walletAddress, amount, comment
                                )
                                backStackEntry.arguments?.clear()
                                model
                            } else if (deeplinkModelFlow != null && !transition.isRunning) {
                                deeplinkModelFlow
                            } else {
                                null
                            },
                            onScanClick = {
                                navController.navigate(QR_SCAN_DESTINATION)
                            },
                            onSettingsClick = {
                                navController.navigate(SETTINGS_DESTINATION)
                            },
                            onSendToAddressClick = {
                                navController.navigate(SEND_ADDRESS_DESTINATION)
                            },
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                    composable(
                        route = WALLET_RECEIVE_DESTINATION,
                        enterTransition = { slideInVertically(tween(500)) { it } },
                        exitTransition = { slideOutVertically(tween(500)) { it } }
                    ) { TonReceiveScreen(tonWalletClient) }
                    composable(
                        route = "$WALLET_TRANSACTION_DETAILS_DESTINATION/{transactionId}",
                        arguments = listOf(navArgument("transactionId") { type = NavType.LongType })
                    ) { entry ->
                        TonTransactionDetailsScreen(
                            tonWalletClient = tonWalletClient,
                            transactionId = entry.arguments?.getLong("transactionId") ?: 0L,
                            onSendToAddressClick = { address ->
                                navController.navigate("$SEND_ADDRESS_DESTINATION?wallet_address=$address")
                            }
                        )
                    }
                    composable(
                        route = "$LOCK_DESTINATION/successDestination={destination}",
                        arguments = listOf(
                            navArgument("destination") {
                                type = NavType.StringType
                            }
                        ),
                        exitTransition = { fadeOut() },
                        popExitTransition = { slideOutHorizontally(tween(500)) { it } }
                    ) { entry ->
                        LockScreen(
                            tonWalletClient = tonWalletClient
                        ) { passcode ->
                            entry.arguments?.getString("destination")?.let { dest ->
                                if (dest == "back") {
                                    navController.popBackStack()
                                } else {
                                    navController.navigate("$dest&passcode=$passcode") {
                                        popUpTo(
                                            if (dest.contains("true")) {
                                                SETTINGS_DESTINATION
                                            } else {
                                                CREATE_WALLET_CONGRATULATIONS_DESTINATION
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    composable(route = QR_SCAN_DESTINATION) { backStackEntry ->
                        TonWalletCameraScreen(
                            tonWalletClient = tonWalletClient,
                            onBackClick = {
                                navController.popBackStack()
                            },
                            onResult = { result ->
                                if (result.startsWith("ton:")) {
                                    backStackEntry.savedStateHandle["qrScanResult"] = result
                                } else if (result.startsWith("tc:") || result.startsWith("https://app.tonkeeper.com/ton-connect?")) {
                                    viewModel.processString(result)
                                }
                                navController.popBackStack()
                            }
                        )
                    }

                    composable(D_APPS_DESTINATION) {
                        DAppsScreen(
                            tonConnectManager = tonWalletClient.tonConnectManager,
                            storage = storage,
                            onBackClick = {
                                navController.popBackStack()
                            }
                        )
                    }

                    createWalletGraph(navController, tonWalletClient)
                    importWalletGraph(navController = navController, tonWalletClient = tonWalletClient)
                    passcodeSetupGraph(tonWalletClient, navController)
                    //sendGraph(navController, tonWalletClient)
                }
            }
        }
    }
}

const val START_DESTINATION = "start"

const val WALLET_MAIN_DESTINATION = "wallet_main"
const val WALLET_RECEIVE_DESTINATION = "wallet_receive"
const val WALLET_TRANSACTION_DETAILS_DESTINATION = "wallet_transaction_details"

const val SETTINGS_DESTINATION = "settings"
const val LOCK_DESTINATION = "lock"
const val QR_SCAN_DESTINATION = "qr_scan"
const val D_APPS_DESTINATION = "d_apps"
