package com.github.tim06.wallet_contest.storage.transaction.msg

import drinkless.org.ton.TonApi

interface MsgData

fun MsgData.toApi(): TonApi.MsgData = when (this) {
    is MsgDataDecryptedText -> toApi()
    is MsgDataEncryptedText -> toApi()
    is MsgDataRaw -> toApi()
    is MsgDataText -> toApi()
    else -> error("Unknown MsgData: $this")
}

fun TonApi.MsgData.toStorage(): MsgData = when (this) {
    is TonApi.MsgDataDecryptedText -> toStorage()
    is TonApi.MsgDataEncryptedText -> toStorage()
    is TonApi.MsgDataRaw -> toStorage()
    is TonApi.MsgDataText -> toStorage()
    else -> error("Unknown TonApi.MsgData: $this")
}