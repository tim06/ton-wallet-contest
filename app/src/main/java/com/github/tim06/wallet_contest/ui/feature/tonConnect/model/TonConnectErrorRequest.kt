package com.github.tim06.wallet_contest.ui.feature.tonConnect.model

@kotlinx.serialization.Serializable
data class TonConnectErrorRequest(
    val id: Long,
    val error: TonConnectError
)
