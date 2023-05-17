package com.github.tim06.wallet_contest.ui.feature.send.amount

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.github.tim06.wallet_contest.R
import com.github.tim06.wallet_contest.ui.components.rlottie.LottieIcon
import com.github.tim06.wallet_contest.ui.theme.InterRegular
import com.github.tim06.wallet_contest.ui.theme.SwitchCheckedColor
import com.github.tim06.wallet_contest.ui.theme.SwitchUncheckedColor
import com.github.tim06.wallet_contest.util.calculateTextWidth

@Composable
fun TonSendAmountSendAllSwitch(
    amount: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val spaceWidth = calculateTextWidth(
        style = MaterialTheme.typography.body2.copy(
            fontFamily = InterRegular
        )
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(spaceWidth)) {
                Text(
                    text = stringResource(id = R.string.send_send_all),
                    style = MaterialTheme.typography.body2.copy(
                        fontFamily = InterRegular,
                        color = Color.Black
                    )
                )
                LottieIcon(
                    modifier = Modifier.size(20.dp),
                    iconSize = DpSize(20.dp, 20.dp),
                    icon = R.raw.main
                )
                Text(
                    text = amount,
                    style = MaterialTheme.typography.body2.copy(
                        fontFamily = InterRegular,
                        color = Color.Black
                    )
                )
            }
            // TODO replace with Telegram switch
            Switch(
                checked = checked,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    uncheckedThumbColor = Color.White,
                    checkedTrackColor = SwitchCheckedColor,
                    uncheckedTrackColor = SwitchUncheckedColor,
                    checkedTrackAlpha = 1f,
                    uncheckedTrackAlpha = 1f
                ),
                onCheckedChange = onCheckedChange
            )
        }
    }
}
