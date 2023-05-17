package com.github.tim06.wallet_contest.ui.feature.main.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.github.tim06.wallet_contest.storage.transaction.RawTransaction
import com.github.tim06.wallet_contest.ton.TonWalletClient
import com.github.tim06.wallet_contest.util.formatCurrency
import org.telegram.time.FastDateFormat
import java.util.Locale

@Composable
fun TonWalletMainHistoryList(
    modifier: Modifier = Modifier,
    transactions: Map<String, List<RawTransaction>>,
    lazyListState: LazyListState,
    onTransactionClick: (Long) -> Unit
) {
    val dateFormatter = remember<FastDateFormat> {
        FastDateFormat.getInstance("HH:mm", Locale.ENGLISH)
    }

    LazyColumn(
        modifier = modifier.navigationBarsPadding(),
        state = lazyListState,
        // TODO check this padding
        contentPadding = PaddingValues(bottom = 60.dp)
    ) {
        transactions.forEach { transactionsSection ->
            item {
                TonWalletMainHistoryDateItem(transactionsSection.key)
            }
            transactionsSection.value.forEachIndexed { index, transaction ->
                item {
                    TonWalletMainHistoryItem(
                        modifier = Modifier.clickable(role = Role.Button) {
                            onTransactionClick.invoke(transaction.transactionId.lt)
                        },
                        income = transaction.isIncome(),
                        time = dateFormatter.format(transaction.utime * 1000),
                        count = transaction.getAmount(),
                        address = transaction.getDestinationOrSourceAddress(),
                        fee = if (transaction.storageFee != 0L) (-transaction.storageFee).formatCurrency(true) else null,
                        message = transaction.getMessage(),
                        isFirst = index == 0
                    )
                }
            }
        }
    }
}