package com.github.tim06.wallet_contest.ui.feature.tonConnect.model.response

@kotlinx.serialization.Serializable
data class TonConnectMethodParamsMessageResponse(
    val address: String,
    val amount: String
)
