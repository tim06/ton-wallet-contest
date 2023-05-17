package com.github.tim06.wallet_contest.ui.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.dialog
import com.github.tim06.wallet_contest.ton.TonWalletClient
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.navigation
import com.github.tim06.wallet_contest.ui.feature.import_wallet.no_words.ImportNoWordsScreen
import com.github.tim06.wallet_contest.ui.feature.import_wallet.success.ImportSuccessScreen
import com.github.tim06.wallet_contest.ui.feature.import_wallet.words.ImportSecretWordsScreen

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.importWalletGraph(
    navController: NavController,
    tonWalletClient: TonWalletClient
) {
    navigation(startDestination = IMPORT_WALLET_SECRET_WORDS_DESTINATION, route = IMPORT_WALLET_DESTINATION) {
        composable(IMPORT_WALLET_SECRET_WORDS_DESTINATION) {
            ImportSecretWordsScreen(
                tonWalletClient = tonWalletClient,
                onBackClick = { navController.popBackStack() },
                onNoWordsClick = { navController.navigate(IMPORT_WALLET_NO_WORDS_DESTINATION) },
                onContinueClick = { key ->
                    navController.navigate("$PASSCODE_PERFECT_DESTINATION?isCreateFlow=false&inputKet=$key")
                }
            )
        }
        composable(IMPORT_WALLET_NO_WORDS_DESTINATION) {
            ImportNoWordsScreen(
                onBackClick = { navController.popBackStack() },
                onEnterWordsClick = { navController.popBackStack() },
                onCreateNewClick = { navController.navigate(CREATE_WALLET_CONGRATULATIONS_DESTINATION) }
            )
        }
        composable(IMPORT_WALLET_SUCCESS_DESTINATION) {
            ImportSuccessScreen(
                onProceedClick = {
                    navController.navigate(WALLET_MAIN_DESTINATION) {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}

const val IMPORT_WALLET_DESTINATION = "import_wallet"
const val IMPORT_WALLET_SECRET_WORDS_DESTINATION = "import_wallet_secret_words"
private const val IMPORT_WALLET_NO_WORDS_DESTINATION = "import_wallet_no_words"
const val IMPORT_WALLET_SUCCESS_DESTINATION = "import_wallet_success"
