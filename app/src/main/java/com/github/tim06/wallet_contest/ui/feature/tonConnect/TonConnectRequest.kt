package com.github.tim06.wallet_contest.ui.feature.tonConnect

@kotlinx.serialization.Serializable
data class TonConnectRequest(
    val manifestUrl: String,
    val items: List<TonConnectRequestItem>
)

@kotlinx.serialization.Serializable
data class TonConnectRequestItem(
    val name: String,
    val payload: String? = null
)