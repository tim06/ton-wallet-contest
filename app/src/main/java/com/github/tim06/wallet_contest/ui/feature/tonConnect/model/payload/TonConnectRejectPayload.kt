package com.github.tim06.wallet_contest.ui.feature.tonConnect.model.payload

@kotlinx.serialization.Serializable
data class TonConnectRejectPayload(
    val code: Int,
    val message: String
): TonConnectPayload
