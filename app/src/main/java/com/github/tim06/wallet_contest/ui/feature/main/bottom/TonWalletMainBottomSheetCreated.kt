package com.github.tim06.wallet_contest.ui.feature.main.bottom

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
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
import com.github.tim06.wallet_contest.ui.theme.RobotoMonoRegular
import com.github.tim06.wallet_contest.ui.theme.SecondaryTextColor

@Composable
fun TonWalletMainBottomSheetCreated(
    modifier: Modifier = Modifier,
    address: String
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            LottieIcon(
                modifier = Modifier.size(100.dp),
                iconSize = DpSize(100.dp, 100.dp),
                icon = R.raw.created
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(id = R.string.wallet_created_title),
                style = MaterialTheme.typography.body1.copy(
                    color = Color.Black
                )
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(id = R.string.wallet_created_description),
                style = MaterialTheme.typography.body2.copy(
                    color = SecondaryTextColor
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = address.take(address.count() / 2) + "\n" + address.takeLast(address.count() / 2),
                maxLines = 2,
                minLines = 2,
                style = MaterialTheme.typography.body2.copy(
                    fontFamily = RobotoMonoRegular,
                    color = Color.Black
                )
            )
        }
    }
}