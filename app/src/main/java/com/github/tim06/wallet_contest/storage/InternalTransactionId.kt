package com.github.tim06.wallet_contest.storage

import drinkless.org.ton.TonApi

@kotlinx.serialization.Serializable
data class InternalTransactionId(
    val lt: Long = 0,
    val hash: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InternalTransactionId

        if (lt != other.lt) return false
        if (hash != null) {
            if (other.hash == null) return false
            if (!hash.contentEquals(other.hash)) return false
        } else if (other.hash != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = lt.hashCode()
        result = 31 * result + (hash?.contentHashCode() ?: 0)
        return result
    }
}

fun TonApi.InternalTransactionId.toStorage(): InternalTransactionId = InternalTransactionId(lt, hash)
fun InternalTransactionId.toApi(): TonApi.InternalTransactionId = TonApi.InternalTransactionId(lt, hash)