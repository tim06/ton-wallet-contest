package com.github.tim06.wallet_contest.storage.transaction.msg

import drinkless.org.ton.TonApi

@kotlinx.serialization.Serializable
data class MsgDataRaw(
    val body: ByteArray,
    val initState: ByteArray
) : MsgData {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MsgDataRaw

        if (!body.contentEquals(other.body)) return false
        if (!initState.contentEquals(other.initState)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = body.contentHashCode()
        result = 31 * result + initState.contentHashCode()
        return result
    }
}

fun TonApi.MsgDataRaw.toStorage(): MsgDataRaw = MsgDataRaw(body, initState)
fun MsgDataRaw.toApi(): TonApi.MsgDataRaw = TonApi.MsgDataRaw(body, initState)