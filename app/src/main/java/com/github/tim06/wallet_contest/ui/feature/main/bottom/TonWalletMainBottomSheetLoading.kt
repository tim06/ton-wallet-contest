package com.github.tim06.wallet_contest.ui.feature.main.bottom

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.github.tim06.wallet_contest.R
import com.github.tim06.wallet_contest.ui.components.rlottie.LottieIcon

@Composable
fun TonWalletMainBottomSheetLoading(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        LottieIcon(
            modifier = Modifier.size(100.dp),
            iconSize = DpSize(100.dp, 100.dp),
            icon = R.raw.loading,
            repeatable = true
        )
    }
}