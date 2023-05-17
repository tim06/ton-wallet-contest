package com.github.tim06.wallet_contest

import java.nio.charset.Charset

fun domainToBytes(domain: String): ByteArray {
    if (domain.isEmpty()) {
        throw IllegalArgumentException("empty domain")
    }
    if (domain == ".") {
        return byteArrayOf(0)
    }

    val lowerCaseDomain = domain.toLowerCase()

    for (i in lowerCaseDomain.indices) {
        if (lowerCaseDomain[i].toInt() <= 32) {
            throw IllegalArgumentException("bytes in range 0..32 are not allowed in domain names")
        }
    }

    for (i in lowerCaseDomain.indices) {
        val s = lowerCaseDomain.substring(i, i + 1)
        for (c in 127..159) { // another control codes range
            if (s == c.toChar().toString()) {
                throw IllegalArgumentException("bytes in range 127..159 are not allowed in domain names")
            }
        }
    }

    val arr = lowerCaseDomain.split(".")

    arr.forEach { part ->
        if (part.isEmpty()) {
            throw IllegalArgumentException("domain name cannot have an empty component")
        }
    }

    var rawDomain = arr.reversed().joinToString("\u0000") + "\u0000"
    if (rawDomain.length < 126) {
        rawDomain = "\u0000$rawDomain"
    }

    return rawDomain.toByteArray(Charset.defaultCharset())
}
