package com.github.tim06.wallet_contest.ui.components.camera

import android.graphics.ImageFormat
import android.net.Uri
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.github.tim06.wallet_contest.ton.TonWalletClient
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import java.nio.ByteBuffer

class QrCodeAnalyzer(
    private val tonWalletClient: TonWalletClient,
    private val onQrCodeScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val supportedImageFormats = listOf(
        ImageFormat.YUV_420_888,
        ImageFormat.YUV_422_888,
        ImageFormat.YUV_444_888
    )

    override fun analyze(image: ImageProxy) {
        if (image.format in supportedImageFormats) {
            val bytes = image.planes.first().buffer.toByteArray()
            val source = PlanarYUVLuminanceSource(
                bytes,
                image.width,
                image.height,
                0,
                0,
                image.width,
                image.height,
                false
            )
            val binaryBmp = BinaryBitmap(HybridBinarizer(source))
            try {
                val result = QRCodeReader().decode(binaryBmp)
                if (result.text.isNotEmpty()) {
                    if (result.text.startsWith("ton://transfer/")) {
                        val uri = Uri.parse(result.text)
                        val path = uri.path?.replace("/", "")
                        if (path != null && tonWalletClient.isValidWalletAddress(path)) {
                            onQrCodeScanned.invoke(result.text.split("/").last())
                        }
                    } else if (result.text.startsWith("tc://?v=2") || result.text.startsWith("https://app.tonkeeper.com/ton-connect?")) {
                        onQrCodeScanned.invoke(result.text)
                    }
                }
            } catch (e: Exception) {
                //e.printStackTrace()
            } finally {
                image.close()
            }
        }
    }

    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()
        return ByteArray(remaining()).also {
            get(it)
        }
    }
}