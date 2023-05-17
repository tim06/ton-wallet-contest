package com.github.tim06.wallet_contest.ui.feature.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.tim06.wallet_contest.ui.theme.RobotoRegular

@Composable
fun WalletSettingsItemTitle(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
    ) {
        Text(
            modifier = Modifier.align(Alignment.BottomStart).padding(start = 20.dp, bottom = 4.dp),
            text = text,
            style = TextStyle(
                fontFamily = RobotoRegular,
                fontWeight = FontWeight.Medium,
                lineHeight = 16.sp,
                fontSize = 15.sp,
                color = MaterialTheme.colors.primary
            )
        )
    }
}