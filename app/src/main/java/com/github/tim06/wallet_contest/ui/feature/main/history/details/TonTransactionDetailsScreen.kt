package com.github.tim06.wallet_contest.ui.feature.main.history.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.github.tim06.wallet_contest.storage.transaction.RawTransaction
import com.github.tim06.wallet_contest.ton.TonWalletClient
import com.github.tim06.wallet_contest.ui.theme.BlackAlpha30

@Composable
fun TonTransactionDetailsScreen(
    tonWalletClient: TonWalletClient,
    transactionId: Long = 0L,
    onSendToAddressClick: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = BlackAlpha30)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        TonTransactionDetailsContent(
            modifier = Modifier.align(Alignment.BottomCenter),
            tonWalletClient = tonWalletClient,
            transactionId = transactionId,
            onSendToAddressClick = onSendToAddressClick
        )
    }
}