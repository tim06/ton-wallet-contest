package com.github.tim06.wallet_contest.ui.feature.tonConnect.model.response

@kotlinx.serialization.Serializable
data class TonConnectMethodParamsResponse(
    val messages: List<TonConnectMethodParamsMessageResponse>,
    val valid_until: Long
)
