package com.github.tim06.wallet_contest.ui.feature.tonConnect.model.response

@kotlinx.serialization.Serializable
data class TonConnectMethodResponse(
    val id: Int,
    val method: String,
    val params: List<String>
)