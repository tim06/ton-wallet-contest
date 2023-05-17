package com.github.tim06.wallet_contest.ui.feature.start

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import com.github.tim06.wallet_contest.R
import com.github.tim06.wallet_contest.ton.TonWalletClient
import com.github.tim06.wallet_contest.ui.components.InfoScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun StartScreen(
    tonWalletClient: TonWalletClient,
    onCreateClick: () -> Unit,
    onImportClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    InfoScreen(
        icon = R.raw.start,
        title = stringResource(id = R.string.start_screen_title),
        description = stringResource(id = R.string.start_screen_description),
        firstButtonText = stringResource(id = R.string.start_screen_create),
        firstButtonClick = {
            onCreateClick.invoke()
            coroutineScope.launch(Dispatchers.IO) {
                //val result = client2.getAccountStatus("EQAnWWaiv3yeu62XslbzpSxoPAf6qK7ojC7GOOcCr0Aiv7hv")
                //walletClient.getBalanceByWalletAddress("EQAnWWaiv3yeu62XslbzpSxoPAf6qK7ojC7GOOcCr0Aiv7hv")
                /*keyResultval keyResult = client2.importWallet("")
                if (keyResult is TonApi.Key) {

                    val addressResult = client2.getAccountAddress(keyResult.publicKey)
                    if (addressResult is TonApi.AccountAddress) {
                        val accountStatusResult = client2.getAccountStatus(addressResult.accountAddress)
                        if (accountStatusResult is TonApi.FullAccountState) {

                        }
                    }
                }*/
            }
        }/*onCreateClick*/,
        secondButtonText = stringResource(id = R.string.start_screen_import),
        secondButtonClick = onImportClick
    )
}