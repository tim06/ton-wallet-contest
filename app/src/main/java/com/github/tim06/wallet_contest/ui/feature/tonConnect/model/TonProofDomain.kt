package com.github.tim06.wallet_contest.ui.feature.tonConnect.model

/**
 * @param lengthBytes AppDomain Length
 * @param value app domain name (as url part, without encoding)
 */
@kotlinx.serialization.Serializable
data class TonProofDomain(
    val lengthBytes: Long,
    val value: String
)