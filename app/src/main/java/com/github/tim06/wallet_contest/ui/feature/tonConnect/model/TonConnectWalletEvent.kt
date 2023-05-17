package com.github.tim06.wallet_contest.ui.feature.tonConnect.model

import com.github.tim06.wallet_contest.ui.feature.tonConnect.model.payload.TonConnectPayload

@kotlinx.serialization.Serializable
data class TonConnectWalletEvent(
    val event: String,
    val id: Long,
    val payload: TonConnectPayload
)