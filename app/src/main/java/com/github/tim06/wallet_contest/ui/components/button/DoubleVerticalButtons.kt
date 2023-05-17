package com.github.tim06.wallet_contest.ui.components.button

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun DoubleVerticalButtons(
    modifier: Modifier = Modifier,
    firstButtonText: String,
    secondButtonText: String,
    fistButtonClick: () -> Unit,
    secondButtonClick: () -> Unit
) {
    Column(
        modifier = modifier.widthIn(min = 200.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TonButton(modifier = Modifier.fillMaxWidth(), text = firstButtonText, click = fistButtonClick)
        TonTextButton(modifier = Modifier.fillMaxWidth(), text = secondButtonText, click = secondButtonClick)
    }
}

@Preview
@Composable
private fun DoubleVerticalButtonsPreview() {
    DoubleVerticalButtons(
        firstButtonText = "Create my wallet",
        secondButtonText = "Import existing wallet",
        fistButtonClick = {},
        secondButtonClick = {}
    )
}