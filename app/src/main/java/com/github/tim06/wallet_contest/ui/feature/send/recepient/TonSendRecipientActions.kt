package com.github.tim06.wallet_contest.ui.feature.send.recepient

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.github.tim06.wallet_contest.R
import com.github.tim06.wallet_contest.ui.theme.InterRegular

@Composable
fun TonSendRecipientActions(
    modifier: Modifier = Modifier,
    onPasteActionClick: () -> Unit,
    onScanActionClick: () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TonSendAction(
            modifier = Modifier.clickable(role = Role.Button, onClick = onPasteActionClick),
            iconRes = R.drawable.ic_paste,
            textRes = R.string.send_paste,
        )
        TonSendAction(
            modifier = Modifier.clickable(role = Role.Button, onClick = onScanActionClick),
            iconRes = R.drawable.ic_scan_mini,
            textRes = R.string.send_scan
        )
    }
}

@Composable
private fun TonSendAction(
    modifier: Modifier = Modifier,
    @DrawableRes iconRes: Int,
    @StringRes textRes: Int
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 8.dp).size(20.dp),
            painter = painterResource(id = iconRes),
            contentDescription = "Action",
            tint = MaterialTheme.colors.primary
        )
        Text(
            modifier = Modifier.padding(end = 8.dp),
            text = stringResource(id = textRes),
            style = MaterialTheme.typography.caption.copy(
                fontFamily = InterRegular,
                color = MaterialTheme.colors.primary
            )
        )
    }
}