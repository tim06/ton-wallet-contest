package com.github.tim06.wallet_contest.ui.feature.send.amount

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.github.tim06.wallet_contest.R
import com.github.tim06.wallet_contest.ui.components.rlottie.LottieIcon
import com.github.tim06.wallet_contest.ui.theme.*

@Composable
fun TonSendAmountInputTextField(
    modifier: Modifier = Modifier,
    showError: Boolean = false,
    amount: String,
    onAmountChange: (String) -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LottieIcon(
                modifier = Modifier.size(44.dp, 56.dp),
                iconSize = DpSize(44.dp, 56.dp),
                icon = R.raw.main
            )
            AmountTextField(
                showError = showError,
                text = amount,
                onValueChange = onAmountChange
            )
        }
        if (showError) {
            Text(
                text = stringResource(id = R.string.send_insufficient_funds),
                style = MainWalletAddressTextStyle.copy(
                    color = ErrorColor
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun AmountTextField(
    text: String,
    showError: Boolean,
    onValueChange: (String) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val colors = TextFieldDefaults.textFieldColors(
        cursorColor = MaterialTheme.colors.primary,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent
    )

    BasicTextField(
        modifier = Modifier.width(IntrinsicSize.Min),
        enabled = false,
        value = text,
        cursorBrush = SolidColor(MaterialTheme.colors.primary),
        textStyle = BalanceBigTextStyle.copy(
            color = if (showError) {
                ErrorColor
            } else {
                Color.Black
            }
        ),
        onValueChange = onValueChange
    ) { innerTextField ->
        TextFieldDefaults.TextFieldDecorationBox(
            value = text,
            visualTransformation = VisualTransformation.None,
            innerTextField = innerTextField,
            singleLine = true,
            enabled = true,
            isError = false,
            interactionSource = interactionSource,
            colors = colors,
            contentPadding = PaddingValues(
                start = 0.dp,
                top = 0.dp,
                end = 0.dp,
                bottom = 0.dp
            ),
            placeholder = {
                Text(
                    text = "0",
                    style = BalanceBigTextStyle.copy(
                        color = BlackAlpha20
                    )
                )
            }
        )
    }
}