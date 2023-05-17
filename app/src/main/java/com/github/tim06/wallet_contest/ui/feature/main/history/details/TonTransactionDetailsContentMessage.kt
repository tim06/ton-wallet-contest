package com.github.tim06.wallet_contest.ui.feature.main.history.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.tim06.wallet_contest.R
import com.github.tim06.wallet_contest.storage.transaction.msg.TransactionMessageState
import com.github.tim06.wallet_contest.ui.feature.main.history.MessageTextStyle
import com.github.tim06.wallet_contest.ui.theme.ChipBackgroundColor

@Composable
fun ColumnScope.TonTransactionDetailsContentMessage(
    modifier: Modifier = Modifier,
    message: TransactionMessageState
) {
    if (message is TransactionMessageState.Decrypting || message is TransactionMessageState.Success) {
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = modifier.background(
                color = ChipBackgroundColor,
                shape = RoundedCornerShape(
                    topStart = 4.dp,
                    topEnd = 10.dp,
                    bottomStart = 10.dp,
                    bottomEnd = 10.dp
                )
            )
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
                text = when (message) {
                    is TransactionMessageState.Decrypting -> {
                        stringResource(id = R.string.transaction_encrypted_message)
                    }
                    is TransactionMessageState.Success -> {
                        message.message
                    }
                    else -> ""
                },
                style = MessageTextStyle
            )
        }
    }
}