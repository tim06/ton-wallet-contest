package com.github.tim06.wallet_contest.ui.feature.send

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.github.tim06.wallet_contest.ui.theme.BlackAlpha70

@Composable
fun TonSendContainer(
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = BlackAlpha70)
            .imePadding()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = MaterialTheme.colors.background,
                    shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)
                )
                .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)),
            content = content
        )
    }
}