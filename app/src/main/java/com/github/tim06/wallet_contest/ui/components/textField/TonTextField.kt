package com.github.tim06.wallet_contest.ui.components.textField

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TextFieldDefaults.indicatorLine
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.github.tim06.wallet_contest.ui.theme.DividerColor1
import com.github.tim06.wallet_contest.ui.theme.TonBlue

@OptIn(ExperimentalMaterialApi::class, ExperimentalTextApi::class)
@Composable
fun TonTextField(
    modifier: Modifier = Modifier,
    text: String,
    number: Int,
    enabled: Boolean,
    onNextClick: () -> Unit,
    onTextChanged: (String) -> Unit
) {
    val destiny = LocalDensity.current

    val prefix by remember(number) { derivedStateOf { "$number: " } }

    val textMeasurer = rememberTextMeasurer()
    val style = MaterialTheme.typography.body2
    val measured = remember(textMeasurer, style, number) {
        if (number > 9) {
            val width = textMeasurer.measure(text = number.toString().first().toString(), style = style).size.width
            with(destiny) {
                width.toDp()
            }
        } else {
            0.dp
        }
    }

    val interactionSource = remember { MutableInteractionSource() }
    val visualTransformation = remember(prefix) { PhraseNumberVisualTransformation(prefix) }

    val colors = TextFieldDefaults.textFieldColors(
        textColor = Color.Black,
        backgroundColor = Color.Transparent,
        focusedIndicatorColor = TonBlue,
        unfocusedIndicatorColor = DividerColor1
    )

    BasicTextField(
        value = text,
        modifier = modifier
            .background(Color.Transparent)
            .indicatorLine(
                enabled = true,
                isError = false,
                interactionSource = interactionSource,
                colors = colors
            ),
        onValueChange = onTextChanged,
        enabled = enabled,
        readOnly = false,
        textStyle = MaterialTheme.typography.body2,
        cursorBrush = SolidColor(MaterialTheme.colors.primary),
        visualTransformation = visualTransformation,
        interactionSource = interactionSource,
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(
            onNext = { onNextClick.invoke() }
        ),
        decorationBox = @Composable { innerTextField ->
            TextFieldDefaults.TextFieldDecorationBox(
                value = text,
                visualTransformation = visualTransformation,
                innerTextField = innerTextField,
                singleLine = true,
                enabled = enabled,
                isError = false,
                interactionSource = interactionSource,
                colors = colors,
                contentPadding = PaddingValues(
                    start = 12.dp - measured,
                    top = 12.dp,
                    end = 12.dp,
                    bottom = 12.dp
                )
            )
        }
    )
}