package com.github.tim06.wallet_contest.ui.components.button

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun TonTextButton(
    modifier: Modifier = Modifier,
    text: String,
    click: () -> Unit,
    content: @Composable RowScope.() -> Unit = {
        Text(
            text = text,
            style = MaterialTheme.typography.caption
        )
    }
) {
    TextButton(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        onClick = click,
        content = content
    )
}

@Preview
@Composable
private fun TonTextButtonPreview() {
    TonTextButton(text = "A really big button text", click = { })
}