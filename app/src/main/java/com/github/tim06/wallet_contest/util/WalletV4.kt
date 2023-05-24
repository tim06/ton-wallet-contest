package com.github.tim06.wallet_contest.util

import com.github.tim06.wallet_contest.ui.feature.tonConnect.base64Url
import org.ton.java.smartcontract.wallet.Options
import org.ton.java.smartcontract.wallet.Wallet
import org.ton.java.utils.Utils

// TODO move to c++ lib when .so lib custom configured
fun getWalletV4RawAddress(publicKey: String): String {
    val options: Options = Options().apply {
        this.publicKey = Utils.hexToBytes(convertPublicKey(publicKey))
        this.wc = 0L
    }

    val wallet = Wallet(org.ton.java.smartcontract.types.WalletVersion.V4R2, options)
    return wallet.create().address.toString(false)
}

fun convertPublicKey(publicKeyBase64: String): String {
    val publicKeyBytes = base64Url(publicKeyBase64)
    if (publicKeyBytes.size != 36) {
        throw IllegalArgumentException("Serialized Ed25519 public key must be exactly 36 bytes long")
    }
    if (publicKeyBytes[0] != 0x3E.toByte() || publicKeyBytes[1] != 0xE6.toByte()) {
        throw IllegalArgumentException("Not a valid public key")
    }
    val hexString = publicKeyBytes.sliceArray(2 until 34)
        .joinToString("") { "%02x".format(it) }
    return hexString
}