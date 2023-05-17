package com.github.tim06.wallet_contest.storage

import com.github.tim06.wallet_contest.storage.transaction.msg.MsgData
import com.github.tim06.wallet_contest.storage.transaction.msg.toApi
import com.github.tim06.wallet_contest.storage.transaction.msg.toStorage
import drinkless.org.ton.TonApi

@kotlinx.serialization.Serializable
data class RawMessage(
    val source: AccountAddress,
    val destination: AccountAddress,
    val value: Long,
    val fwdFee: Long,
    val ihrFee: Long,
    val createdLt: Long,
    val bodyHash: ByteArray?,
    val msgData: MsgData
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RawMessage

        if (source != other.source) return false
        if (destination != other.destination) return false
        if (value != other.value) return false
        if (fwdFee != other.fwdFee) return false
        if (ihrFee != other.ihrFee) return false
        if (createdLt != other.createdLt) return false
        if (bodyHash != null) {
            if (other.bodyHash == null) return false
            if (!bodyHash.contentEquals(other.bodyHash)) return false
        } else if (other.bodyHash != null) return false
        if (msgData != other.msgData) return false

        return true
    }

    override fun hashCode(): Int {
        var result = source.hashCode()
        result = 31 * result + destination.hashCode()
        result = 31 * result + value.hashCode()
        result = 31 * result + fwdFee.hashCode()
        result = 31 * result + ihrFee.hashCode()
        result = 31 * result + createdLt.hashCode()
        result = 31 * result + (bodyHash?.contentHashCode() ?: 0)
        result = 31 * result + msgData.hashCode()
        return result
    }
}

fun TonApi.RawMessage.toStorage(): RawMessage = RawMessage(
    source = source.toStorage(),
    destination = destination.toStorage(),
    value = value,
    fwdFee = fwdFee,
    ihrFee = ihrFee,
    createdLt = createdLt,
    bodyHash = bodyHash,
    msgData = msgData.toStorage()
)

fun RawMessage.toApi(): TonApi.RawMessage = TonApi.RawMessage(
    source.toApi(),
    destination.toApi(),
    value,
    fwdFee,
    ihrFee,
    createdLt,
    bodyHash,
    msgData.toApi()
)