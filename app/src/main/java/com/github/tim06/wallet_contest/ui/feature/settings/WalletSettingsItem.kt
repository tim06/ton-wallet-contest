package com.github.tim06.wallet_contest.ui.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.github.tim06.wallet_contest.ui.theme.DividerColor1
import com.github.tim06.wallet_contest.ui.theme.InterRegular
import com.github.tim06.wallet_contest.ui.theme.SwitchCheckedColor
import com.github.tim06.wallet_contest.ui.theme.SwitchUncheckedColor

@Composable
fun WalletSettingsItem(
    modifier: Modifier = Modifier,
    text: String,
    showDivider: Boolean = true,
    textColor: Color = Color.Black,
    rightContent: @Composable () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.body2.copy(
                    fontFamily = InterRegular,
                    color = textColor
                )
            )
            rightContent.invoke()
        }
        if (showDivider) {
            Divider(modifier = Modifier.fillMaxWidth().align(Alignment.BottomStart), color = DividerColor1, thickness = 0.5.dp)
        }
    }
}

@Composable
fun WalletSettingsItemText(
    modifier: Modifier = Modifier,
    text: String,
    rightText: String
) {
    WalletSettingsItem(
        modifier = modifier,
        text = text
    ) {
        Text(
            text = rightText,
            style = MaterialTheme.typography.body2.copy(
                fontFamily = InterRegular,
                color = MaterialTheme.colors.primary
            )
        )
    }
}

@Composable
fun WalletSettingsItemSwitch(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    WalletSettingsItem(text = text) {
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