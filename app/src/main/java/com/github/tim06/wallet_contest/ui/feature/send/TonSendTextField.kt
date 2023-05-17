package com.github.tim06.wallet_contest.ui.feature.send

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TextFieldDefaults.indicatorLine
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.github.tim06.wallet_contest.ui.theme.BlackAlpha50
import com.github.tim06.wallet_contest.ui.theme.DividerColor1

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TonSendTextField(
    modifier: Modifier = Modifier,
    text: TextFieldValue,
    hint: String,
    onTextChanged: (TextFieldValue) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    BasicTextField(
        modifier = modifier
            .indicatorLine(
                enabled = true,
                isError = false,
                interactionSource = interactionSource,
                colors = TextFieldDefaults.textFieldColors(unfocusedIndicatorColor = DividerColor1)
            )
            .background(
                color = TextFieldDefaults
                    .textFieldColors()
                    .backgroundColor(true).value,
            )
            .fillMaxWidth(),
        value = text,
        onValueChange = onTextChanged,
        singleLine = false,
        interactionSource = interactionSource,
        cursorBrush = SolidColor(MaterialTheme.colors.primary),
        textStyle = MaterialTheme.typography.body2.copy(
            color = Color.Black
        )
    ) { innerTextField ->
        TextFieldDefaults.TextFieldDecorationBox(
            value = text.text,
            innerTextField = innerTextField,
            enabled = true,
            singleLine = false,
            visualTransformation = VisualTransformation.None,
            interactionSource = interactionSource,
            placeholder = {
                Text(
                    text = hint,
                    style = MaterialTheme.typography.body2.copy(
                        color = BlackAlpha50
                    )
                )
            },
            contentPadding = PaddingValues(
                start = 0.dp,
                top = 0.dp,
                end = 0.dp,
                bottom = 14.dp
            )
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TonSendTextField(
    modifier: Modifier = Modifier,
    text: String,
    hint: String,
    onTextChanged: (String) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    BasicTextField(
        modifier = modifier
            .indicatorLine(
                enabled = true,
                isError = false,
                interactionSource = interactionSource,
                colors = TextFieldDefaults.textFieldColors(unfocusedIndicatorColor = DividerColor1)
            )
            .background(
                color = TextFieldDefaults
                    .textFieldColors()
                    .backgroundColor(true).value,
            )
            .fillMaxWidth(),
        value = text,
        onValueChange = onTextChanged,
        singleLine = false,
        interactionSource = interactionSource,
        cursorBrush = SolidColor(MaterialTheme.colors.primary),
        textStyle = MaterialTheme.typography.body2.copy(
            color = Color.Black
        )
    ) { innerTextField ->
        TextFieldDefaults.TextFieldDecorationBox(
            value = text,
            innerTextField = innerTextField,
            enabled = true,
            singleLine = false,
            visualTransformation = VisualTransformation.None,
            interactionSource = interactionSource,
            placeholder = {
                Text(
                    text = hint,
                    style = MaterialTheme.typography.body2.copy(
                        color = BlackAlpha50
                    )
                )
            },
            contentPadding = PaddingValues(
                start = 0.dp,
                top = 0.dp,
                end = 0.dp,
                bottom = 14.dp
            )
        )
    }
}