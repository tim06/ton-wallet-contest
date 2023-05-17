package com.github.tim06.wallet_contest.storage.transaction.msg

import drinkless.org.ton.TonApi

@kotlinx.serialization.Serializable
data class MsgDataDecryptedText(
    val text: ByteArray
) : MsgData {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MsgDataDecryptedText

        if (!text.contentEquals(other.text)) return false

        return true
    }

    override fun hashCode(): Int {
        return text.contentHashCode()
    }
}

fun TonApi.MsgDataDecryptedText.toStorage(): MsgDataDecryptedText = MsgDataDecryptedText(text)
fun MsgDataDecryptedText.toApi(): TonApi.MsgDataDecryptedText = TonApi.MsgDataDecryptedText(text)