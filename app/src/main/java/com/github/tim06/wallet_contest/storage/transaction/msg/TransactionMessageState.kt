package com.github.tim06.wallet_contest.storage.transaction.msg

sealed interface TransactionMessageState {
    object Empty: TransactionMessageState
    object Decrypting: TransactionMessageState
    data class Success(val message: String): TransactionMessageState
}