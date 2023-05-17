package com.github.tim06.wallet_contest.ui.feature.receive

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.tim06.wallet_contest.R
import com.github.tim06.wallet_contest.ton.TonWalletClient
import com.github.tim06.wallet_contest.ui.components.button.TonButton
import com.github.tim06.wallet_contest.ui.components.title.TonTitle
import com.github.tim06.wallet_contest.ui.theme.BlackAlpha30
import com.github.tim06.wallet_contest.ui.theme.BlackAlpha70
import com.github.tim06.wallet_contest.ui.theme.BottomSheetBgColor
import com.github.tim06.wallet_contest.ui.theme.RobotoMonoRegular
import com.github.tim06.wallet_contest.ui.theme.SecondaryTextColor
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

@Composable
fun TonReceiveScreen(
    client: TonWalletClient,
    viewModel: TonReceiveViewModel = viewModel(
        factory = TonReceiveViewModelFactory(
            tonWalletClient = client
        )
    )
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    val walletAddress by viewModel.walletAddress.collectAsState(initial = "")
    val qrData by viewModel.qrData.collectAsState(initial = null)

    val qrBitmap = remember(qrData) {
        if (qrData.isNullOrEmpty().not()) {
            runCatching {
                val hints = mapOf(
                    EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
                    EncodeHintType.MARGIN to 0
                )
                QRCodeWriter().encode(
                    qrData,
                    BarcodeFormat.QR_CODE,
                    with(density) { 160.dp.roundToPx() },
                    with(density) { 160.dp.roundToPx() },
                    hints,
                    null,
                    context
                )
            }.getOrNull()
        } else {
            null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = BottomSheetBgColor)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .background(
                    color = MaterialTheme.colors.background,
                    shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)
                )
        ) {
            TonTitle(text = stringResource(id = R.string.receive_ton_title))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = stringResource(id = R.string.receive_ton_description),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.body2.copy(
                    color = SecondaryTextColor
                )
            )

            Spacer(modifier = Modifier.height(26.dp))
            qrBitmap?.let {
                Image(
                    modifier = Modifier
                        .size(160.dp)
                        .align(Alignment.CenterHorizontally),
                    bitmap = it.asImageBitmap(),
                    contentDescription = "qr"
                )
            }
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = walletAddress.take(walletAddress.count() / 2) + "\n" + walletAddress.takeLast(
                    walletAddress.count() / 2
                ),
                maxLines = 2,
                minLines = 2,
                style = MaterialTheme.typography.body2.copy(
                    fontFamily = RobotoMonoRegular,
                    color = Color.Black
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            TonButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                text = stringResource(id = R.string.receive_ton_share),
                click = {
                    qrBitmap?.let {
                        client.shareBitmap(it, qrData)
                    }
                }
            )
        }
    }
}
