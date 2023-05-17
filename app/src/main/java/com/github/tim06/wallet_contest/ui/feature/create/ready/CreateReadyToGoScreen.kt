package com.github.tim06.wallet_contest.ui.feature.create.ready

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.github.tim06.wallet_contest.R
import com.github.tim06.wallet_contest.ui.components.InfoScreen

@Composable
fun CreateReadyToGoScreen(
    onWalletClick: () -> Unit
) {
    InfoScreen(
        icon = R.raw.success,
        title = stringResource(id = R.string.create_ready_to_go_title),
        description = stringResource(id = R.string.create_ready_to_go_description),
        firstButtonText = stringResource(id = R.string.create_ready_to_go_button),
        firstButtonClick = onWalletClick
    )
}