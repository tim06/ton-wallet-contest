package com.github.tim06.wallet_contest.ui.feature.main.header

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.tim06.wallet_contest.R
import com.github.tim06.wallet_contest.ui.components.button.TonButton

@Composable
fun WalletMainDoubleHorizontalButtons(
    modifier: Modifier = Modifier,
    receiveClick: () -> Unit,
    sendClick: () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TonWalletButton(modifier = Modifier.fillMaxWidth().weight(1f), icon = R.drawable.ic_receive, text = R.string.wallet_main_receive, click = receiveClick)
        TonWalletButton(modifier = Modifier.fillMaxWidth().weight(1f), icon = R.drawable.ic_send, text = R.string.wallet_main_send, click = sendClick)
    }
}

@Composable
private fun TonWalletButton(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int,
    @StringRes text: Int,
    click: () -> Unit
) {
    TonButton(modifier = modifier, click = click) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(painter = painterResource(id = icon), contentDescription = stringResource(id = text))
            Text(
                text = stringResource(id = text),
                style = MaterialTheme.typography.button
            )
        }
    }
}