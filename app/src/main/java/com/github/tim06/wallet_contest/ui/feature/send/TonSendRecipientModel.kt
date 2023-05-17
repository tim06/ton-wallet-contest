package com.github.tim06.wallet_contest.ui.feature.send

@kotlinx.serialization.Serializable
data class TonSendRecipientModel(
    val address: String,
    val tonDnsAddress: String? = null
)