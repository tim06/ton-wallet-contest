package com.github.tim06.wallet_contest.ui.feature.send.pending

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.tim06.wallet_contest.R
import com.github.tim06.wallet_contest.ui.components.InfoScreen
import com.github.tim06.wallet_contest.ui.components.button.TonButton
import com.github.tim06.wallet_contest.ui.components.text.IconWithText
import com.github.tim06.wallet_contest.ui.feature.send.TonSendContainer
import com.github.tim06.wallet_contest.ui.theme.RobotoMonoRegular
import com.github.tim06.wallet_contest.util.formatCurrency
import com.github.tim06.wallet_contest.util.splitAddressToTwoLines

@Composable
fun TonSendPendingScreen(
    viewModel: TonSendPendingViewModel,
    address: String? = null,
    amount: Long? = null,
    onCloseClick: () -> Unit,
    onViewWalletClick: () -> Unit
) {
    val success by viewModel.isSendSuccess.collectAsState()
    val error by viewModel.isSendError.collectAsState()

    TonSendContainer {
        AnimatedVisibility(
            visible = success,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            SuccessContent(
                address = address.orEmpty(),
                amount = amount ?: 0L,
                onCloseClick = onCloseClick,
                onViewWalletClick = onViewWalletClick
            )
        }
        AnimatedVisibility(
            visible = success.not(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            PendingContent(
                onCloseClick = onCloseClick,
                onViewWalletClick = onViewWalletClick
            )
        }
    }
}

@Composable
private fun PendingContent(
    onCloseClick: () -> Unit,
    onViewWalletClick: () -> Unit
) {
    InfoScreen(
        top = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopStart) {
                IconButton(
                    modifier = Modifier.padding(4.dp),
                    onClick = onCloseClick
                ) {
                    Icon(painter = painterResource(id = R.drawable.ic_close), contentDescription = "Close")
                }
            }
        },
        center = {
            IconWithText(
                modifier = Modifier.padding(horizontal = 40.dp),
                icon = R.raw.waiting_ton,
                title = stringResource(id = R.string.send_sending_title),
                description = stringResource(id = R.string.send_sending_description)
            )
        },
        bottom = {
            TonButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                click = onViewWalletClick,
                text = stringResource(id = R.string.send_sending_button)
            )
        }
    )
}

@Composable
private fun SuccessContent(
    address: String,
    amount: Long,
    onCloseClick: () -> Unit,
    onViewWalletClick: () -> Unit
) {
    InfoScreen(
        top = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopStart) {
                IconButton(
                    modifier = Modifier.padding(4.dp),
                    onClick = onCloseClick
                ) {
                    Icon(painter = painterResource(id = R.drawable.ic_close), contentDescription = "Close")
                }
            }
        },
        center = {
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                IconWithText(
                    modifier = Modifier.padding(horizontal = 40.dp),
                    icon = R.raw.success,
                    title = stringResource(id = R.string.send_sending_done_title),
                    description = stringResource(id = R.string.send_sending_done_description, amount.formatCurrency())
                )
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = address.splitAddressToTwoLines(),
                    style = MaterialTheme.typography.body2.copy(
                        fontFamily = RobotoMonoRegular,
                        color = Color.Black
                    )
                )
            }
        },
        bottom = {
            TonButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                click = onViewWalletClick,
                text = stringResource(id = R.string.send_sending_done_button)
            )
        }
    )
}
