package com.github.tim06.wallet_contest.ui.feature.send.amount

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.tim06.wallet_contest.R
import com.github.tim06.wallet_contest.ton.TonWalletClient
import com.github.tim06.wallet_contest.ui.components.button.TonButton
import com.github.tim06.wallet_contest.ui.components.keyboard.CustomPhoneKeyboardView
import com.github.tim06.wallet_contest.ui.components.topBar.TonTopAppBar
import com.github.tim06.wallet_contest.ui.feature.import_wallet.ImportViewModelFactory
import com.github.tim06.wallet_contest.ui.feature.send.TonSendContainer
import com.github.tim06.wallet_contest.ui.feature.send.TonSendRecipientModel
import com.github.tim06.wallet_contest.util.toTonLong
import com.github.tim06.wallet_contest.util.transformAddress
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Composable
fun TonSendAmountScreen(
    tonWalletClient: TonWalletClient,
    recipientAddress: String? = null,
    amount: Long? = null,
    viewModel: TonSendAmountViewModel = viewModel(
        factory = TonSendAmountViewModelFactory(
            tonWalletClient,
            amount
        )
    ),
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onContinueClick: (Long) -> Unit
) {
    val walletBalance by viewModel.walletBalance.collectAsState()
    val enteredAmount by viewModel.enteredAmount.collectAsState()
    val sendAllChecked by viewModel.sendAllChecked.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val showError by viewModel.error.collectAsState(initial = false)

    val keyboardListener = remember {
        object : CustomPhoneKeyboardView.OnKeyboardQueryListener {
            override fun onClick(number: String) {
                viewModel.onNumberClick(number)
            }
            override fun onDeleteClick() {
                viewModel.onDeleteClick()
            }

            override fun onBiometricClick() = Unit
        }
    }

    val recipientData = remember(recipientAddress) {
        if (recipientAddress != null) {
            val model = Json.decodeFromString<TonSendRecipientModel>(recipientAddress)
            TonSendAmountRecipientData(
                model.address.transformAddress(),
                model.tonDnsAddress
            )
        } else {
            null
        }
    }
    TonSendContainer {
        Column {
            TonTopAppBar(title = stringResource(id = R.string.send_title), backClick = onBackClick)
            TonSendAmountRecipient(
                data = recipientData,
                onEditClick = onEditClick
            )
            Spacer(modifier = Modifier.height(50.dp))
            TonSendAmountInputTextField(
                showError = showError,
                amount = enteredAmount
            ) {
                //amount = it
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
        ) {
            TonSendAmountSendAllSwitch(
                amount = walletBalance.toString(),
                checked = sendAllChecked,
                onCheckedChange = viewModel::onSendAllCheckChanged
            )
            Spacer(modifier = Modifier.height(8.dp))
            TonButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                text = stringResource(id = R.string.send_continue),
                click = { onContinueClick.invoke(enteredAmount.toTonLong()) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(CustomPhoneKeyboardView.KEYBOARD_HEIGHT_DP.dp),
                factory = { context ->
                    CustomPhoneKeyboardView(
                        context,
                        keyboardListener,
                        false,
                        true,
                        false
                    )
                },
                update = {
                    it.setInputEnabled(!loading)
                }
            )
        }
    }
}