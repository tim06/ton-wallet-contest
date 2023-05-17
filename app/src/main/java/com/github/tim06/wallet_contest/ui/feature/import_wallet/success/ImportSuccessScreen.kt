package com.github.tim06.wallet_contest.ui.feature.import_wallet.success

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.github.tim06.wallet_contest.R
import com.github.tim06.wallet_contest.ui.components.InfoScreen

@Composable
fun ImportSuccessScreen(
    onProceedClick: () -> Unit
) {
    InfoScreen(
        icon = R.raw.congratulations,
        title = stringResource(id = R.string.import_success_title),
        firstButtonText = stringResource(id = R.string.import_success_button),
        firstButtonClick = onProceedClick
    )
}