package com.github.tim06.wallet_contest.ui.feature.tonConnect.model

@kotlinx.serialization.Serializable
data class TonConnectError(
    val code: Int,
    val message: String
)
