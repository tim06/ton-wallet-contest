package com.github.tim06.wallet_contest.ui.feature.send.recepient

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.tim06.wallet_contest.R
import com.github.tim06.wallet_contest.ton.TonWalletClient
import com.github.tim06.wallet_contest.ui.components.button.TonButton
import com.github.tim06.wallet_contest.ui.components.title.TonTitle
import com.github.tim06.wallet_contest.ui.feature.passcode_setup.CreatePasscodeViewModel
import com.github.tim06.wallet_contest.ui.feature.passcode_setup.CreatePasscodeViewModelFactory
import com.github.tim06.wallet_contest.ui.feature.send.TonSendContainer
import com.github.tim06.wallet_contest.ui.feature.send.TonSendTextField
import com.github.tim06.wallet_contest.ui.theme.SecondaryTextColor
import com.github.tim06.wallet_contest.ui.theme.SnackbarBackgroundColor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Composable
fun TonSendRecipientScreen(
    tonWalletClient: TonWalletClient,
    recipientAddress: String? = null,
    viewModel: TonSendRecipientViewModel = viewModel(
        factory = TonSendRecipientViewModelFactory(tonWalletClient, recipientAddress)
    ),
    onScanClick: () -> Unit,
    onContinueClick: (String) -> Unit
) {
    val clipboard = LocalClipboardManager.current
    val address by viewModel.address.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val success by viewModel.success.collectAsState()
    val recent by viewModel.recentSendTransactions.collectAsState(initial = emptyList())

    LaunchedEffect(key1 = success) {
        if (success) {
            viewModel.recipientModel?.let {
                onContinueClick.invoke(Json.encodeToString(it))
            }
        }
    }

    LaunchedEffect(key1 = recipientAddress) {
        viewModel.onAddressChanged(recipientAddress.orEmpty())
    }

    TonSendContainer {
        Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.height(8.dp))
            TonTitle(text = stringResource(id = R.string.send_title))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                modifier = Modifier.padding(start = 20.dp),
                text = stringResource(id = R.string.send_text_field_title),
                style = MaterialTheme.typography.body2.copy(
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colors.primary
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            TonSendTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                text = address,
                hint = stringResource(id = R.string.send_text_field_hint),
                onTextChanged = viewModel::onAddressChanged
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                text = stringResource(id = R.string.send_text_field_description),
                style = MaterialTheme.typography.body2.copy(
                    fontSize = 13.sp,
                    lineHeight = 16.sp,
                    color = SecondaryTextColor
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            TonSendRecipientActions(
                modifier = Modifier.padding(start = 12.dp),
                onPasteActionClick = {
                    if (clipboard.hasText()) {
                        viewModel.onAddressChanged(clipboard.getText()?.text.orEmpty())
                    }
                },
                onScanActionClick = onScanClick
            )
            Spacer(modifier = Modifier.height(24.dp))
            TonSendRecipientRecent(
                recentItems = recent,
                onItemClick = viewModel::onAddressChanged
            )
        }

        TonSendButton(
            loading = loading,
            enabled = address.isNotEmpty() && address.isNotBlank(),
            onClick = viewModel::onButtonClick
        )
        AnimatedVisibility(
            modifier = Modifier.align(Alignment.BottomStart),
            visible = error.isNotEmpty()
        ) {
            TonSendSnackBar()
        }
    }
}

@Composable
private fun BoxScope.TonSendButton(
    loading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    TonButton(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
        enabled = enabled,
        click = onClick
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = stringResource(id = R.string.send_continue),
                style = MaterialTheme.typography.button
            )
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.CenterEnd),
                    strokeWidth = 2.dp,
                    color = Color.White,
                    strokeCap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
private fun BoxScope.TonSendSnackBar() {
    Snackbar(
        modifier = Modifier.padding(8.dp),
        elevation = 0.dp,
        shape = RoundedCornerShape(6.dp),
        backgroundColor = SnackbarBackgroundColor
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_warning),
                contentDescription = "Warning",
                tint = Color.White
            )
            Column {
                Text(
                    text = stringResource(id = R.string.send_invalid_address),
                    style = MaterialTheme.typography.body2.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        color = Color.White
                    )
                )
                Text(
                    text = stringResource(id = R.string.send_invalid_address_description),
                    style = MaterialTheme.typography.body2.copy(
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        color = Color.White
                    )
                )
            }
        }
    }
}