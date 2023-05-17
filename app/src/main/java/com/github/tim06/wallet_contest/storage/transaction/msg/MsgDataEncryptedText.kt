package com.github.tim06.wallet_contest.storage.transaction.msg

import drinkless.org.ton.TonApi

@kotlinx.serialization.Serializable
data class MsgDataEncryptedText(
    var text: ByteArray?
): MsgData {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MsgDataEncryptedText

        if (text != null) {
            if (other.text == null) return false
            if (!text.contentEquals(other.text)) return false
        } else if (other.text != null) return false

        return true
    }

    override fun hashCode(): Int {
        return text?.contentHashCode() ?: 0
    }
}

fun TonApi.MsgDataEncryptedText.toStorage(): MsgDataEncryptedText = MsgDataEncryptedText(text)
fun MsgDataEncryptedText.toApi(): TonApi.MsgDataEncryptedText = TonApi.MsgDataEncryptedText(text)