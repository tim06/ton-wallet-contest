package com.github.tim06.wallet_contest.ui.feature.tonConnect

import com.github.tim06.wallet_contest.ui.feature.tonConnect.model.TonConnectConnectionData

sealed interface TonConnectEvent {
    data class TonConnectTransactionRequest(
        val data: TonConnectConnectionData,
        val from: String,
        val recipient: String,
        val amount: String
    ) : TonConnectEvent
}