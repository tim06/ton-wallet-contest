package com.github.tim06.wallet_contest.ui.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.*
import com.github.tim06.wallet_contest.ton.TonWalletClient
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.navigation
import com.github.tim06.wallet_contest.ui.feature.create.perfrect.CreatePerfectScreen
import com.github.tim06.wallet_contest.ui.feature.passcode_setup.CreatePasscodeScreen

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.passcodeSetupGraph(client: TonWalletClient, navController: NavController) {
        composable(
            route = "$PASSCODE_PERFECT_DESTINATION?isCreateFlow={isCreateFlow}&inputKet={inputKey}",
            arguments = listOf(
                navArgument("isCreateFlow") {
                    type = NavType.BoolType
                },
                navArgument("inputKey") {
                    type = NavType.StringType
                }
            )
        ) { entry ->
            CreatePerfectScreen(
                tonWalletClient = client,
                inputKey = entry.arguments?.getString("inputKey"),
                onSetPasscodeClick = {
                    navController.navigate("$PASSCODE_SETUP_DESTINATION?isCreateFlow=${entry.arguments?.getBoolean("isCreateFlow")}")
                },
                onBackClickListener = { navController.popBackStack() }
            )
        }
        composable(
            route = "$PASSCODE_SETUP_DESTINATION?isCreateFlow={isCreateFlow}",
            arguments = listOf(
                navArgument("isCreateFlow") {
                    type = NavType.BoolType
                }
            )
        ) { entry ->
            CreatePasscodeScreen(client) { passcode ->
                navController.navigate("$PASSCODE_CONFIRM_DESTINATION?passcode=$passcode&isCreateFlow=${entry.arguments?.getBoolean("isCreateFlow")}")
            }
        }
        composable(
            route = "$PASSCODE_CONFIRM_DESTINATION?passcode={passcode}&isCreateFlow={isCreateFlow}",
            arguments = listOf(
                navArgument("isCreateFlow") {
                    type = NavType.BoolType
                },
                navArgument("passcode") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val isCreateFlow = backStackEntry.arguments?.getBoolean("isCreateFlow") ?: false
            CreatePasscodeScreen(
                client = client,
                savedPasscode = backStackEntry.arguments?.getString("passcode"),
                onSuccess = {
                    navController.navigate(
                        route = if (isCreateFlow) {
                            CREATE_WALLET_READY_DESTINATION
                        } else {
                            IMPORT_WALLET_SUCCESS_DESTINATION
                        }
                    ) {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    //}
}

const val PASSCODE_INITIALIZATION_DESTINATION = "passcode_init"
const val PASSCODE_PERFECT_DESTINATION = "passcode_perfect"
const val PASSCODE_SETUP_DESTINATION = "passcode_setup"
const val PASSCODE_CONFIRM_DESTINATION = "passcode_confirm"

const val CREATE_FLOW_PASSCODE_ARGUMENT_KEY = "CREATE_FLOW_PASSCODE_ARGUMENT_KEY"
const val PASSCODE_CONFIRM_ARGUMENT_KEY = "PASSCODE_CONFIRM_ARGUMENT_KEY"