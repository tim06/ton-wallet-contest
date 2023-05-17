package com.github.tim06.wallet_contest.ui.feature.tonConnect

import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

// Функция для создания TonProofItem подписи
fun createTonProofItemSignature(
    walletAddress: ByteArray,
    appDomain: String,
    timestamp: Long,
    payload: ByteArray,
    privateKey: ByteArray
): ByteArray {
    val message = createMessage(
        address = Address(0, walletAddress),
        appDomain = AppDomain(21, appDomain),
        timestamp = timestamp,
        payload = payload
    )

    // Вычисляем SHA256 хеш от сообщения с префиксом "ton-connect"
    val sha256 = SHA256Digest()
    val prefixByteArray = byteArrayOf(0xFF.toByte(), 0xFF.toByte())
    sha256.update(prefixByteArray, 0, prefixByteArray.size)
    val prefixStringByteArray = "ton-connect".toByteArray(Charsets.UTF_8)
    sha256.update(prefixStringByteArray, 0, prefixStringByteArray.size)
    sha256.update(sha256(message), 0, sha256.digestSize)
    val hash = ByteArray(sha256.digestSize)
    sha256.doFinal(hash, 0)

    val signer = Ed25519Signer()
    signer.init(true, Ed25519PrivateKeyParameters(privateKey, 0))
    signer.update(hash, 0, hash.size)
    return signer.generateSignature()
}

fun createMessage(
    address: Address,
    appDomain: AppDomain,
    timestamp: Long,
    payload: ByteArray
): ByteArray {
    val prefix = "ton-proof-item-v2/"
    val wc = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(address.workchain).array()
    val addressBytes = address.hash
    val appDomainBytes = appDomain.value.toByteArray(Charsets.UTF_8)
    val appDomainLength = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
        .putInt(appDomain.length).array()
    val ts = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(timestamp).array()
    val message = prefix.toByteArray(Charsets.UTF_8) +
            wc + addressBytes + appDomainLength + appDomainBytes + ts + payload
    return message
}

data class Address(val workchain: Int, val hash: ByteArray)

data class AppDomain(val length: Int, val value: String)

fun sha256(bytes: ByteArray): ByteArray = MessageDigest.getInstance("SHA-256").digest(bytes)


private val DIGITS = "0123456789abcdef".toCharArray()

fun hex(bytes: ByteArray): String = buildString(bytes.size * 2) {
    bytes.forEach { byte ->
        val b = byte.toInt() and 0xFF
        append(DIGITS[b shr 4])
        append(DIGITS[b and 0x0F])
    }
}

/**
 * Decode bytes from HEX string. It should be no spaces and `0x` prefixes.
 */
fun hex(s: String): ByteArray {
    val result = ByteArray(s.length / 2)
    for (idx in result.indices) {
        val srcIdx = idx * 2
        val high = s[srcIdx].toString().toInt(16) shl 4
        val low = s[srcIdx + 1].toString().toInt(16)
        result[idx] = (high or low).toByte()
    }
    return result
}

@OptIn(ExperimentalEncodingApi::class)
fun base64(byteArray: ByteArray): String = Base64.Default.encode(byteArray)
@OptIn(ExperimentalEncodingApi::class)
fun base64(string: String): ByteArray = Base64.Default.decode(string)