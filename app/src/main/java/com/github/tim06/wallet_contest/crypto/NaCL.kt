package com.github.tim06.wallet_contest.crypto

import org.libsodium.jni.Sodium
import org.libsodium.jni.SodiumConstants
import org.libsodium.jni.crypto.Random
import org.libsodium.jni.keys.KeyPair

fun generateX25519KeyPair(): Pair<ByteArray, ByteArray> {
    val encryptionKeyPair = KeyPair()
    return Pair(encryptionKeyPair.privateKey.toBytes(), encryptionKeyPair.publicKey.toBytes())
}

// Шифрование сообщения
fun encryptMessage(message: ByteArray, recipientPublicKey: ByteArray, senderPrivateKey: ByteArray): ByteArray {
    val nonce = Random().randomBytes(SodiumConstants.NONCE_BYTES)
    val ciphertext = ByteArray(message.size + Sodium.crypto_box_macbytes())

    Sodium.crypto_box_easy(ciphertext, message, message.size,
        nonce, recipientPublicKey, senderPrivateKey)

    return nonce + ciphertext
}

// Дешифрование сообщения
fun decryptMessage(encryptedMessage: ByteArray, senderPublicKey: ByteArray, recipientPrivateKey: ByteArray): ByteArray {
    val nonce = encryptedMessage.sliceArray(0 until Sodium.crypto_box_noncebytes())
    val ciphertext = encryptedMessage.sliceArray(Sodium.crypto_box_noncebytes() until encryptedMessage.size)

    val decrypted = ByteArray(ciphertext.size - Sodium.crypto_box_macbytes())

    if (Sodium.crypto_box_open_easy(decrypted, ciphertext, ciphertext.size,
            nonce, senderPublicKey, recipientPrivateKey) != 0) {
        throw RuntimeException("Could not decrypt message")
    }

    return decrypted
}