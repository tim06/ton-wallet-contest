package com.github.tim06.wallet_contest.ui.feature.main.history.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.tim06.wallet_contest.R
import com.github.tim06.wallet_contest.storage.transaction.RawTransaction
import com.github.tim06.wallet_contest.ui.theme.MainWalletAddressTextStyle
import com.github.tim06.wallet_contest.ui.theme.SecondaryTextColor
import org.telegram.time.FastDateFormat
import java.util.*

@Composable
fun TonTransactionDetailsContentStatus(
    modifier: Modifier = Modifier,
    transaction: RawTransaction
) {
    val dayFormat = remember {
        FastDateFormat.getInstance("MMM dd, yyyy", Locale.ENGLISH)
    }
    val timeFormat = remember {
        FastDateFormat.getInstance("HH:mm", Locale.ENGLISH)
    }
    val transactionDate = stringResource(
        id = R.string.transaction_date,
        dayFormat.format(transaction.utime * 1000),
        timeFormat.format(transaction.utime * 1000)
    )
    val status = remember {
        derivedStateOf {
            // TODO check for canceled status
            if (transaction.isPending()) {
                TransactionStatus.Pending
            } else {
                TransactionStatus.Success(transactionDate)
            }
        }
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(
                size = if (status.value is TransactionStatus.Pending) 10.dp else 0.dp
            ),
            strokeWidth = 1.5.dp,
            strokeCap = StrokeCap.Round,
            color = MaterialTheme.colors.primary
        )
        Text(
            text = when (val state = status.value) {
                is TransactionStatus.Canceled -> stringResource(id = R.string.transaction_canceled)
                is TransactionStatus.Pending -> stringResource(id = R.string.transaction_pending)
                is TransactionStatus.Success -> state.date/*stringResource(
                    id = R.string.transaction_date,
                    status.date,
                    status.time
                )*/
            },
            style = MainWalletAddressTextStyle.copy(
                color = when (status.value) {
                    is TransactionStatus.Canceled -> MaterialTheme.colors.error
                    is TransactionStatus.Pending -> MaterialTheme.colors.primary
                    is TransactionStatus.Success -> SecondaryTextColor
                }
            )
        )
    }
}

sealed class TransactionStatus {
    data class Success(val date: String) : TransactionStatus()
    object Pending : TransactionStatus()
    object Canceled : TransactionStatus()
}