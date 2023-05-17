package com.github.tim06.wallet_contest

import com.github.tim06.wallet_contest.crypto.decryptMessage
import com.github.tim06.wallet_contest.crypto.encryptMessage
import org.junit.Assert.assertEquals
import org.junit.Test
import org.libsodium.jni.NaCl
import org.libsodium.jni.keys.KeyPair

class CryptoBoxTest {

    @Test
    fun `test encryption and decryption`() {
        NaCl.sodium()
        // Generate client key pair
        val clientKeyPair = KeyPair()

        val clientPrivateKey = clientKeyPair.privateKey
        val clientPublicKey = clientKeyPair.publicKey

        // Generate message to encrypt
        val message = "Hello, world!".toByteArray()

        // Encrypt message
        val encryptedMessage = encryptMessage(message, clientPublicKey.toBytes(), clientPrivateKey.toBytes())

        // Decrypt message
        val decryptedMessage = decryptMessage(encryptedMessage, clientPublicKey.toBytes(), clientPrivateKey.toBytes())

        assertEquals(message, decryptedMessage)
    }
}
