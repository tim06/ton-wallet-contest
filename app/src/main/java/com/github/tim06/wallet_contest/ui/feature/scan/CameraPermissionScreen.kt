package com.github.tim06.wallet_contest.ui.feature.scan

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.tim06.wallet_contest.R
import com.github.tim06.wallet_contest.ui.components.button.TonButton
import com.github.tim06.wallet_contest.ui.components.text.TitleWithDescription
import com.github.tim06.wallet_contest.ui.components.topBar.TonTopAppBar

@Composable
fun CameraPermissionScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    Box(modifier = Modifier
        .fillMaxSize()
        .background(color = Color.Black)) {
        TonTopAppBar(title = "", backgroundColor = Color.Black, backClick = onBackClick)
        TitleWithDescription(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(40.dp),
            title = stringResource(id = R.string.scan_qr_permission_title),
            description = stringResource(id = R.string.scan_qr_permission_description),
            titleColor = Color.White,
            descriptionColor = Color.White
        )
        TonButton(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .width(200.dp)
                .padding(bottom = 100.dp),
            click = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:" + context.packageName)
                context.startActivity(intent)
            },
            text = stringResource(id = R.string.scan_qr_permission_open_settings)
        )
    }
}