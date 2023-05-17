package com.github.tim06.wallet_contest.ui.components.button

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

@Composable
fun TonLoadingButton(
    modifier: Modifier = Modifier,
    text: String,
    loading: Boolean = false,
    click: () -> Unit
) {
    TonButton(
        modifier = modifier,
        click = click
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = text,
                style = MaterialTheme.typography.button
            )
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.CenterEnd),
                    strokeWidth = 2.dp,
                    color = Color.White,
                    strokeCap = StrokeCap.Round
                )
            }
        }
    }
}