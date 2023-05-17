package com.github.tim06.wallet_contest.ui.feature.tonConnect.model

@kotlinx.serialization.Serializable
data class TonCheckProofRequest(
    val address: String,
    val network: String = "-239",
    val proof: TonProof
)