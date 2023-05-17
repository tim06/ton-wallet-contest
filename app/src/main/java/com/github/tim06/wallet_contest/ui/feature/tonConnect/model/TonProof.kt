package com.github.tim06.wallet_contest.ui.feature.tonConnect.model

/**
 *
 * @param timestamp 64-bit unix epoch time of the signing operation (seconds)
 * @param domain
 * @param signature base64-encoded signature
 * @param payload payload from the request
 */
@kotlinx.serialization.Serializable
data class TonProof(
    val timestamp: Long,
    val domain: TonProofDomain,
    val signature: String,
    val payload: String? = null
)
