package com.github.tim06.wallet_contest.ui.feature.send.confirm

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.tim06.wallet_contest.R
import com.github.tim06.wallet_contest.ui.components.row.TonRow
import com.github.tim06.wallet_contest.ui.theme.InterRegular
import com.github.tim06.wallet_contest.ui.theme.RobotoMonoRegular
import com.github.tim06.wallet_contest.ui.theme.RobotoRegular
import com.github.tim06.wallet_contest.util.transformAddress

@Composable
fun TonSendConfirmRows(
    modifier: Modifier = Modifier,
    recipientAddress: String? = null,
    amountExtra: String? = null,
    feeExtra: String? = null
) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Text(
                modifier = Modifier.padding(start = 20.dp, bottom = 4.dp),
                text = stringResource(id = R.string.transaction_details),
                style = TextStyle(
                    fontFamily = RobotoRegular,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 16.sp,
                    fontSize = 15.sp,
                    color = MaterialTheme.colors.primary
                )
            )
        }
        TonRow(
            leftText = stringResource(id = R.string.send_details_recipient),
            rightText = recipientAddress?.transformAddress().orEmpty(),
            rightTextFont = RobotoMonoRegular
        )
        TonRow(
            showIcon = true,
            leftText = stringResource(id = R.string.send_details_amount),
            rightText = amountExtra.orEmpty(),
            rightTextFont = InterRegular
        )
        TonRow(
            showIcon = true,
            showDivider = false,
            rightTextLoading = feeExtra == null || feeExtra.isEmpty(),
            leftText = stringResource(id = R.string.send_details_fee),
            rightText = feeExtra.orEmpty(),
            rightTextFont = InterRegular
        )
    }
}