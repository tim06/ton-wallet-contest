package com.github.tim06.wallet_contest.storage

@kotlinx.serialization.Serializable
data class WalletData(
    val address: String,
    val publicKey: String? = null,
    val walletVersion: WalletVersion,
    val lastTransactionId: InternalTransactionId,
    val balance: Long
)