package com.github.tim06.wallet_contest.ui.components.title

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TonTitle(
    modifier: Modifier = Modifier,
    text: String
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier.padding(start = 20.dp, top = 12.dp, bottom = 12.dp),
            text = text,
            style = MaterialTheme.typography.h6
        )
    }
}