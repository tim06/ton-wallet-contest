package com.github.tim06.wallet_contest.ui.feature.tonConnect.model

@kotlinx.serialization.Serializable
data class TonProofItemReplySuccess(
    override val name: String = "ton_proof",
    val proof: TonProof
): ConnectItemReply