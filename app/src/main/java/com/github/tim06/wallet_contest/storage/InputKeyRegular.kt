package com.github.tim06.wallet_contest.storage

import drinkless.org.ton.TonApi

@kotlinx.serialization.Serializable
data class InputKeyRegular(
    val key: Key,
    val localPassword: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InputKeyRegular

        if (key != other.key) return false
        if (!localPassword.contentEquals(other.localPassword)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = key.hashCode()
        result = 31 * result + localPassword.contentHashCode()
        return result
    }
}

@kotlinx.serialization.Serializable
data class Key(
    val publicKey: String,
    val secret: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Key

        if (publicKey != other.publicKey) return false
        if (!secret.contentEquals(other.secret)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = publicKey.hashCode()
        result = 31 * result + secret.contentHashCode()
        return result
    }
}

fun InputKeyRegular.toTonApiKeyRegular() =
    TonApi.InputKeyRegular(
        TonApi.Key(key.publicKey, key.secret),
        localPassword
    )

fun TonApi.InputKeyRegular.toStorage() = InputKeyRegular(
    key = Key(key.publicKey, key.secret),
    localPassword = localPassword
)
