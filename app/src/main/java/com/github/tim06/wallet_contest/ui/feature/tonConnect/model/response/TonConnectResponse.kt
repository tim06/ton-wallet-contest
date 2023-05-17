package com.github.tim06.wallet_contest.ui.feature.tonConnect.model.response

@kotlinx.serialization.Serializable
data class TonConnectResponse(
    val from: String,
    val message: String
)
