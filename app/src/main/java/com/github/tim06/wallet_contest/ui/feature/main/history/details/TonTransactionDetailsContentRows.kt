package com.github.tim06.wallet_contest.ui.feature.main.history.details

import android.util.Base64
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
import com.github.tim06.wallet_contest.storage.transaction.RawTransaction
import com.github.tim06.wallet_contest.ui.components.row.TonRow
import com.github.tim06.wallet_contest.ui.theme.InterRegular
import com.github.tim06.wallet_contest.ui.theme.InterSemibold
import com.github.tim06.wallet_contest.ui.theme.RobotoMonoRegular
import com.github.tim06.wallet_contest.ui.theme.RobotoRegular
import com.github.tim06.wallet_contest.util.transformAddress

@Composable
fun TonTransactionDetailsContentRows(
    modifier: Modifier = Modifier,
    transaction: RawTransaction,
    isIncome: Boolean = true
) {
    val nickName by remember { mutableStateOf("") }
    Column(modifier = modifier) {
        TonTransactionDetailsContentRowsTitle()
        if (nickName.isNullOrEmpty().not()) {
            TonRow(
                leftText = stringResource(id = if (isIncome) R.string.transaction_sender else R.string.transaction_recipient),
                rightText = nickName
            )
        }
        TonRow(
            leftText = stringResource(id = if (isIncome) R.string.transaction_sender_address else R.string.transaction_recipient_address),
            rightText = transaction.getDestinationOrSourceAddress().transformAddress(),
            rightTextFont = RobotoMonoRegular
        )
        TonRow(
            leftText = stringResource(id = R.string.transaction_transaction),
            rightText = Base64.encodeToString(transaction.transactionId.hash, Base64.DEFAULT).trim().transformAddress(6),
            rightTextFont = InterRegular
        )
        TonTransactionDetailsContentRowsViewExplorer()
    }
}

@Composable
private fun TonTransactionDetailsContentRowsTitle() {
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
}

@Composable
private fun TonTransactionDetailsContentRowsViewExplorer(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            modifier = Modifier.padding(start = 20.dp),
            text = stringResource(id = R.string.transaction_view_explorer),
            style = MaterialTheme.typography.body2.copy(
                color = MaterialTheme.colors.primary
            )
        )
    }
}