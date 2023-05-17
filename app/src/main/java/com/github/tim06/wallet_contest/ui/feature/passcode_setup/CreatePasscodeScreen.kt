package com.github.tim06.wallet_contest.ui.feature.passcode_setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.tim06.wallet_contest.R
import com.github.tim06.wallet_contest.ton.TonWalletClient
import com.github.tim06.wallet_contest.ui.feature.passcode.Passcode

@Composable
fun CreatePasscodeScreen(
    client: TonWalletClient,
    savedPasscode: String? = null,
    viewModel: CreatePasscodeViewModel = viewModel(
        factory = CreatePasscodeViewModelFactory(client, savedPasscode)
    ),
    onSuccess: (String) -> Unit
) {
    val currentPasscode by viewModel.passcode.collectAsState()
    val passcodeCount by viewModel.passcodeCount.collectAsState()
    val success by viewModel.successAnimation.collectAsState()
    val error by viewModel.errorAnimation.collectAsState()

    Passcode(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colors.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        title = stringResource(id = if (viewModel.isConfirm) R.string.create_passcode_confirm else R.string.create_passcode_title),
        showBackIcon = true,
        withBiometric = false,
        dotsCount = passcodeCount,
        filledCount = currentPasscode.count(),
        showSuccessAnimation = success,
        showPasscodeOptions = viewModel.isConfirm.not(),
        showErrorAnimation = error,
        onPasscodeOptionsChanged = viewModel::onCountChanged,
        onNewDigitClick = viewModel::onNewDigitInPasscode,
        onDeleteDigitClick = viewModel::onDeleteDigit,
        onErrorAnimationEnd = viewModel::onErrorAnimationEnd,
        onSuccessAnimationEnd = {
            onSuccess.invoke(currentPasscode)
        }
    )
}

