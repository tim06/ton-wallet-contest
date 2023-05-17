package com.github.tim06.wallet_contest.ton

import android.util.Log
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.URL
import kotlin.coroutines.resume

suspend fun downloadFileToString(url: String): String = suspendCancellableCoroutine { continuation ->
    downloadFileToString(url, continuation::resume)
}

fun downloadFileToString(url: String, onSuccess: (String) -> Unit) {
    var outbuf: ByteArrayOutputStream? = null
    var httpConnectionStream: InputStream? = null
    try {
        val downloadUrl = URL(url)
        val httpConnection = downloadUrl.openConnection()
        httpConnection.addRequestProperty(
            "User-Agent",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 10_0 like Mac OS X) AppleWebKit/602.1.38 (KHTML, like Gecko) Version/10.0 Mobile/14A5297c Safari/602.1"
        )
        httpConnection.connectTimeout = 1000
        httpConnection.readTimeout = 2000
        httpConnection.connect()
        httpConnectionStream = httpConnection.getInputStream()

        outbuf = ByteArrayOutputStream()

        val data = ByteArray(1024 * 32)
        while (true) {
            val read = httpConnectionStream.read(data)
            if (read > 0) {
                outbuf.write(data, 0, read)
            } else if (read == -1) {
                break
            } else {
                break
            }
        }

        onSuccess.invoke(String(outbuf.toByteArray()))
    } catch (e: Exception) {
        Log.e("downloadFileToString", e.message.orEmpty())
    } finally {
        runCatching {
            httpConnectionStream?.close()
        }.onFailure { e ->
            Log.e("downloadFileToString", e.message.orEmpty())
        }
        runCatching { outbuf?.close() }
    }
}