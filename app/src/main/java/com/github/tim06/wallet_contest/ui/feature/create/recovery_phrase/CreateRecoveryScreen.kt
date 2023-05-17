package com.github.tim06.wallet_contest.ui.feature.create.recovery_phrase

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.tim06.wallet_contest.R
import com.github.tim06.wallet_contest.ton.TonWalletClient
import com.github.tim06.wallet_contest.ui.components.ScrollableTitleContainer
import com.github.tim06.wallet_contest.ui.components.button.TonButton
import com.github.tim06.wallet_contest.ui.components.dialog.TonDialog
import com.github.tim06.wallet_contest.util.SystemBarIconsDark

@Composable
fun CreateRecoveryScreen(
    tonWalletClient: TonWalletClient,
    inputKeyRegular: String? = null,
    isStandalone: Boolean = true,
    viewModel: RecoveryPhraseViewModel = viewModel(
        factory = RecoveryPhraseViewModelFactory(tonWalletClient, inputKeyRegular)
    ),
    onDoneClick: (String) -> Unit,
    backClickListener: () -> Unit
) {
    val words by viewModel.secretWords.collectAsState()

    var showDialog by remember { mutableStateOf(false) }

    SystemBarIconsDark(isDark = true)
    ScrollableTitleContainer(
        title = R.string.create_recovery_title,
        description = stringResource(id = R.string.create_recovery_description),
        icon = R.raw.recovery_phrase,
        backClick = backClickListener
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        CreateRecoveryTable(
            modifier = Modifier.padding(horizontal = 40.dp),
            items = words
        )
        if (isStandalone.not()) {
            Spacer(modifier = Modifier.height(44.dp))
            TonButton(
                modifier = Modifier
                    .widthIn(min = 200.dp)
                    .padding(top = 4.dp, bottom = 56.dp),
                text = stringResource(id = R.string.create_recovery_done),
                click = { onDoneClick.invoke(viewModel.randomWords) }
            )
        }
    }
    if (showDialog) {
        SureDoneDialog(true) {
            showDialog = false
        }
    }
}

@Composable
private fun SureDoneDialog(
    withSkip: Boolean,
    dismiss: () -> Unit
) {
    TonDialog(
        title = R.string.create_recovery_sure_done_dialog_title,
        description = R.string.create_recovery_sure_done_dialog_description,
        leftButtonText = if (withSkip) R.string.create_recovery_sure_done_dialog_skip else null,
        leftButtonClick = {},
        rightButtonText = R.string.create_recovery_sure_done_dialog_ok,
        rightButtonClick = dismiss,
        dismissRequest = dismiss
    )
}