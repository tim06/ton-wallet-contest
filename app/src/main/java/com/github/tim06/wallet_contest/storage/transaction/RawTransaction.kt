package com.github.tim06.wallet_contest.storage.transaction

import com.github.tim06.wallet_contest.storage.*
import com.github.tim06.wallet_contest.storage.transaction.msg.*
import drinkless.org.ton.TonApi
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

@kotlinx.serialization.Serializable
data class RawTransaction(
    val address: AccountAddress,
    val utime: Long,
    val data: ByteArray,
    val transactionId: InternalTransactionId,
    val fee: Long,
    val storageFee: Long,
    val otherFee: Long,
    val inMsg: RawMessage? = null,
    val outMsgs: List<RawMessage> = emptyList()
) {

    fun getDestinationOrSourceAddress(): String {
        return if (isIncome()) {
            inMsg?.source?.accountAddress.orEmpty()
        } else {
            outMsgs.firstOrNull()?.destination?.accountAddress.orEmpty()
        }
    }

    fun getAmount(): Long {
        var result = 0L
        if (inMsg != null) {
            result += inMsg.value
        }
        if (outMsgs.isNotEmpty()) {
            result -= outMsgs.sumOf { it.value }
        }
        return result
    }

    fun getMsg(): MsgData? {
        val msg = if (isIncome()) {
            inMsg?.msgData
        } else {
            outMsgs.lastOrNull()?.msgData
        }
        return msg
    }

    fun getMessage(): TransactionMessageState {
        val msg = if (isIncome()) {
            inMsg?.msgData
        } else {
            outMsgs.lastOrNull()?.msgData
        }
        return when (msg) {
            is MsgDataRaw -> TransactionMessageState.Empty
            is MsgDataText -> {
                val textFromBytes = String(msg.text, 0, msg.text.size, StandardCharsets.UTF_8)
                if (textFromBytes.isNotEmpty()) {
                    TransactionMessageState.Success(textFromBytes)
                } else {
                    TransactionMessageState.Empty
                }
            }
            is MsgDataDecryptedText -> {
                val textFromBytes = String(msg.text, 0, msg.text.size, StandardCharsets.UTF_8)
                if (textFromBytes.isNotEmpty()) {
                    TransactionMessageState.Success(textFromBytes)
                } else {
                    TransactionMessageState.Empty
                }
            }
            is MsgDataEncryptedText -> TransactionMessageState.Decrypting
            else -> TransactionMessageState.Empty
        }
    }

    fun isIncome(): Boolean {
        return getAmount() > 0
    }

    fun isPending(): Boolean {
        return transactionId.lt == 0L
    }

    fun isEmpty(): Boolean {
        val hasMessage = inMsg?.msgData !is MsgDataRaw || outMsgs.any { it.msgData !is MsgDataRaw }
        val isSourceEmpty = inMsg?.source?.accountAddress?.isEmpty() ?: true
        return !hasMessage && isSourceEmpty && getAmount() == 0L
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RawTransaction

        if (address != other.address) return false
        if (utime != other.utime) return false
        if (!data.contentEquals(other.data)) return false
        if (transactionId != other.transactionId) return false
        if (fee != other.fee) return false
        if (storageFee != other.storageFee) return false
        if (otherFee != other.otherFee) return false
        if (inMsg != other.inMsg) return false
        if (outMsgs != other.outMsgs) return false

        return true
    }

    override fun hashCode(): Int {
        var result = address.hashCode()
        result = 31 * result + utime.hashCode()
        result = 31 * result + data.contentHashCode()
        result = 31 * result + transactionId.hashCode()
        result = 31 * result + fee.hashCode()
        result = 31 * result + storageFee.hashCode()
        result = 31 * result + otherFee.hashCode()
        result = 31 * result + (inMsg?.hashCode() ?: 0)
        result = 31 * result + outMsgs.hashCode()
        return result
    }

}

fun TonApi.RawTransaction.toStorage(): RawTransaction = RawTransaction(
    address = address.toStorage(),
    utime = utime,
    data = data,
    transactionId = transactionId.toStorage(),
    fee = fee,
    storageFee = storageFee,
    otherFee = otherFee,
    inMsg = inMsg.toStorage(),
    outMsgs = outMsgs.map { it.toStorage() }
)
fun RawTransaction.toApi(): TonApi.RawTransaction = TonApi.RawTransaction(
    address.toApi(),
    utime,
    data,
    transactionId.toApi(),
    fee,
    storageFee,
    otherFee,
    inMsg?.toApi(),
    outMsgs.map { it.toApi() }.toTypedArray()
)