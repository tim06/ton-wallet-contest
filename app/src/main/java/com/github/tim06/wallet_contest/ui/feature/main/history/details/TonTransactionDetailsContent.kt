package com.github.tim06.wallet_contest.ui.feature.main.history.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.tim06.wallet_contest.R
import com.github.tim06.wallet_contest.storage.transaction.RawTransaction
import com.github.tim06.wallet_contest.ton.TonWalletClient
import com.github.tim06.wallet_contest.ui.components.button.TonButton
import com.github.tim06.wallet_contest.ui.components.title.TonTitle
import com.github.tim06.wallet_contest.ui.theme.SecondaryTextColor
import com.github.tim06.wallet_contest.util.formatCurrency

@Composable
fun TonTransactionDetailsContent(
    modifier: Modifier = Modifier,
    tonWalletClient: TonWalletClient,
    transactionId: Long = 0L,
    viewModel: TonTransactionDetailsViewModel = viewModel(
        factory = TonTransactionDetailsViewModelFactory(tonWalletClient)
    ),
    onSendToAddressClick: (String) -> Unit
) {
    LaunchedEffect(key1 = transactionId) {
        viewModel.loadTransaction(transactionId)
    }
    val transactionVm by viewModel.transaction.collectAsState()
    val transactionStatus by remember { mutableStateOf<TransactionStatus>(TransactionStatus.Success("Sept 6, 2022")) }

    transactionVm?.let { transaction ->
        Box(
            modifier = modifier.background(
                color = MaterialTheme.colors.background,
                shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)
            )
        ) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                TonTitle(text = stringResource(id = R.string.transaction_title))
                Spacer(modifier = Modifier.height(20.dp))
                TonTransactionDetailsContentCount(transaction = transaction)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(
                        id = R.string.transaction_fee,
                        transaction.storageFee.formatCurrency(false)
                    ),
                    style = MaterialTheme.typography.body2.copy(
                        lineHeight = 18.sp,
                        color = SecondaryTextColor
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                TonTransactionDetailsContentStatus(transaction = transaction)
                TonTransactionDetailsContentMessage(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    message = transaction.getMessage()
                )
                Spacer(modifier = Modifier.height(12.dp))
                TonTransactionDetailsContentRows(
                    transaction = transaction
                )
                Spacer(modifier = Modifier.height(24.dp))
                TonButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    click = {
                        onSendToAddressClick.invoke(transaction.getDestinationOrSourceAddress())
                    },
                    text = stringResource(
                        id = if (transactionStatus is TransactionStatus.Canceled) {
                            R.string.transaction_retry_send_ton
                        } else {
                            R.string.transaction_send_ton
                        }
                    )
                )
            }
        }
    }
}