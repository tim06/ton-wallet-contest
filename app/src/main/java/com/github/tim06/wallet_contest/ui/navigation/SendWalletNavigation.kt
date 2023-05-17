package com.github.tim06.wallet_contest.ui.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.github.tim06.wallet_contest.storage.InputKeyRegular
import com.github.tim06.wallet_contest.ton.TonWalletClient
import com.github.tim06.wallet_contest.ui.feature.main.TonWalletMainScreenViewModel
import com.github.tim06.wallet_contest.ui.feature.send.TonSendRecipientModel
import com.google.accompanist.navigation.animation.navigation
import com.github.tim06.wallet_contest.ui.feature.send.amount.TonSendAmountScreen
import com.github.tim06.wallet_contest.ui.feature.send.confirm.TonSendConfirmScreen
import com.github.tim06.wallet_contest.ui.feature.send.pending.TonSendPendingScreen
import com.github.tim06.wallet_contest.ui.feature.send.pending.TonSendPendingViewModelFactory
import com.github.tim06.wallet_contest.ui.feature.send.recepient.TonSendRecipientScreen
import com.github.tim06.wallet_contest.ui.feature.send.success.TonSendSuccessScreen
import com.github.tim06.wallet_contest.util.toTonLong
import com.google.accompanist.navigation.animation.composable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.sendGraph(
    navController: NavController,
    tonWalletClient: TonWalletClient,
    onScanOpenClick: () -> Unit,
    onClose: () -> Unit
) {
    val backClick: () -> Unit = { navController.popBackStack() }
    navigation(
        route = "send",
        startDestination = SEND_ADDRESS_DESTINATION,
        enterTransition = { slideInHorizontally(tween(500)) { it } },
        //popEnterTransition = { slideInHorizontally(tween(500)) { it } }
    ) {
        composable(
            route = SEND_ADDRESS_DESTINATION,
            arguments = listOf(
                navArgument("wallet_address") {
                    nullable = true
                    type = NavType.StringType
                }
            )
        ) { entry ->
            val walletAddress = if (entry.savedStateHandle.contains("wallet_address")) {
                entry.savedStateHandle.get<String>("wallet_address")
            } else if (entry.arguments?.containsKey("wallet_address") == true) {
                entry.arguments?.getString("wallet_address")
            } else {
                null
            }
            TonSendRecipientScreen(
                tonWalletClient = tonWalletClient,
                recipientAddress = walletAddress,
                onScanClick = onScanOpenClick,
                onContinueClick = { address ->
                    navController.navigate("$SEND_AMOUNT_DESTINATION?wallet_address=${address}")
                }
            )
        }

        composable(
            route = "$SEND_ADDRESS_DESTINATION?wallet_address={wallet_address}",
            arguments = listOf(
                navArgument("wallet_address") {
                    nullable = true
                    type = NavType.StringType
                }
            )
        ) { entry ->
            val walletAddress = if (entry.savedStateHandle.contains("wallet_address")) {
                entry.savedStateHandle.get<String>("wallet_address")
            } else if (entry.arguments?.containsKey("wallet_address") == true) {
                entry.arguments?.getString("wallet_address")
            } else {
                null
            }
            TonSendRecipientScreen(
                tonWalletClient = tonWalletClient,
                recipientAddress = walletAddress,
                onScanClick = onScanOpenClick,
                onContinueClick = { address ->
                    navController.navigate("$SEND_AMOUNT_DESTINATION?wallet_address=${address}")
                }
            )
        }
        composable(
            route = "$SEND_AMOUNT_DESTINATION?wallet_address={wallet_address}",
            arguments = listOf(
                navArgument("wallet_address") { type = NavType.StringType }
            )
        ) { entry ->
            TonSendAmountScreen(
                tonWalletClient = tonWalletClient,
                recipientAddress = entry.arguments?.getString("wallet_address"),
                onBackClick = {
                    navController.popBackStack()
                },
                onEditClick = backClick,
                onContinueClick = { amount ->
                    navController.navigate(
                        route = "$SEND_CONFIRM_DESTINATION?wallet_address=${entry.arguments?.getString("wallet_address")}&amount=$amount"
                    )
                }
            )
        }
        composable(
            route = "$SEND_CONFIRM_DESTINATION?wallet_address={wallet_address}&amount={amount}&comment={comment}",
            arguments = listOf(
                navArgument("wallet_address") { type = NavType.StringType },
                navArgument("amount") { type = NavType.StringType },
                navArgument("comment") {
                    nullable = true
                    type = NavType.StringType
                }
            )
        ) { entry ->
            TonSendConfirmScreen(
                tonWalletClient = tonWalletClient,
                address = entry.arguments?.getString("wallet_address"),
                amount = entry.arguments?.getString("amount"),
                commentExtra = entry.arguments?.getString("comment"),
                onBackClick = backClick,
                onConfirmClick = { message ->
                    val destination = entry.arguments?.getString("wallet_address")?.let { Json.decodeFromString<TonSendRecipientModel>(it).address }
                    navController.navigate("$LOCK_DESTINATION?destination=${destination}&amount=${entry.arguments?.getString("amount")?.toTonLong()}&comment=$message")
                }
            )
        }

        composable(
            route = "$SEND_PENDING_DESTINATION&destination={destination}&amount={amount}&comment={comment}",
            arguments = listOf(
                navArgument("destination") {
                    type = NavType.StringType
                },
                navArgument("amount") {
                    type = NavType.LongType
                },
                navArgument("comment") {
                    nullable = true
                    type = NavType.StringType
                }
            )
        ) { entry ->
            TonSendPendingScreen(
                viewModel = viewModel(
                    factory = TonSendPendingViewModelFactory(
                        tonWalletClient = tonWalletClient,
                        destination = entry.arguments?.getString("destination"),
                        amount = entry.arguments?.getLong("amount"),
                        comment = entry.arguments?.getString("comment")
                    )
                ),
                address = entry.arguments?.getString("destination"),
                amount = entry.arguments?.getLong("amount"),
                onCloseClick = onClose,
                onViewWalletClick = onClose
            )
        }
        composable(SEND_SUCCESS_DESTINATION) {
            TonSendSuccessScreen(
                onViewWalletClick = onClose,
                onCloseClick = onClose
            )
        }
    }
}

const val SEND_DESTINATION = "send"
const val SEND_ADDRESS_DESTINATION = "send_address"
const val SEND_AMOUNT_DESTINATION = "send_amount"
const val SEND_CONFIRM_DESTINATION = "send_confirm"
const val SEND_PENDING_DESTINATION = "send_pending"
const val SEND_SUCCESS_DESTINATION = "send_success"