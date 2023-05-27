package com.github.tim06.wallet_contest.ui.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.github.tim06.wallet_contest.ton.TonWalletClient
import com.github.tim06.wallet_contest.ui.components.swipetoback.SwipeToBack
import com.github.tim06.wallet_contest.ui.feature.create.congratulations.CreateCongratulationsScreen
import com.github.tim06.wallet_contest.ui.feature.create.ready.CreateReadyToGoScreen
import com.github.tim06.wallet_contest.ui.feature.create.recovery_phrase.CreateRecoveryScreen
import com.github.tim06.wallet_contest.ui.feature.create.test_time.CreateTestTimeScreen
import com.google.accompanist.navigation.animation.composable
import kotlinx.serialization.json.Json

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.createWalletGraph(
    navController: NavController,
    tonWalletClient: TonWalletClient
) {
    val backClick: () -> Unit = { navController.popBackStack() }
    composable(CREATE_WALLET_CONGRATULATIONS_DESTINATION) {
        SwipeToBack(onSwipeBack = navController::popBackStack) {
            CreateCongratulationsScreen(
                tonWalletClient = tonWalletClient,
                onProceedClick = { key ->
                    navController.navigate(
                        route = "$CREATE_WALLET_RECOVERY_PHRASES_DESTINATION&standalone=false&passcode=$key"
                    )
                },
                backClickListener = backClick
            )
        }
    }
    composable(
        route = "$CREATE_WALLET_RECOVERY_PHRASES_DESTINATION&standalone={standalone}&passcode={passcode}",
        arguments = listOf(
            navArgument("standalone") { type = NavType.BoolType },
            navArgument("passcode") { type = NavType.StringType }
        )
    ) { entry ->
        SwipeToBack(onSwipeBack = { navController.popBackStack() }) {
            CreateRecoveryScreen(
                tonWalletClient = tonWalletClient,
                inputKeyRegular = entry.arguments?.getString("passcode"),
                isStandalone = entry.arguments?.getBoolean("standalone") ?: true,
                onDoneClick = { randomWordsSerialized ->
                    navController.navigate(
                        route = "$CREATE_WALLET_TEST_TIME_DESTINATION&key=${entry.arguments?.getString("passcode")}&words=$randomWordsSerialized"
                    )
                },
                backClickListener = backClick
            )
        }
    }
    composable(
        route = "$CREATE_WALLET_TEST_TIME_DESTINATION&key={key}&words={words}",
        arguments = listOf(
            navArgument("key") { type = NavType.StringType },
            navArgument("words") { type = NavType.StringType }
        )
    ) { entry ->
        SwipeToBack(onSwipeBack = navController::popBackStack) {
            CreateTestTimeScreen(
                randomWords = entry.arguments?.getString("words")?.let { Json.decodeFromString(it) }
                    ?: emptyList(),
                onContinueClick = {
                    navController.navigate(
                        route = "$PASSCODE_PERFECT_DESTINATION?isCreateFlow=true&inputKet=${
                            entry.arguments?.getString(
                                "key"
                            )
                        }"
                    )
                },
                backClickListener = backClick
            )
        }
    }

    composable(CREATE_WALLET_READY_DESTINATION) {
        CreateReadyToGoScreen(
            onWalletClick = {
                navController.navigate(WALLET_MAIN_DESTINATION) {
                    popUpTo(navController.graph.id) {
                        inclusive = true
                    }
                }
            }
        )
    }
}

const val CREATE_WALLET_CONGRATULATIONS_DESTINATION = "create_wallet_congratulations"
const val CREATE_WALLET_RECOVERY_PHRASES_DESTINATION = "create_wallet_recovery_phrases"
const val CREATE_WALLET_TEST_TIME_DESTINATION = "create_wallet_test_time"
const val CREATE_WALLET_READY_DESTINATION = "create_wallet_ready"