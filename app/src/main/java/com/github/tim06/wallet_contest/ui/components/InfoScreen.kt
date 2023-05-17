package com.github.tim06.wallet_contest.ui.components

import androidx.annotation.RawRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.tim06.wallet_contest.ui.components.button.DoubleVerticalButtons
import com.github.tim06.wallet_contest.ui.components.button.TonButton
import com.github.tim06.wallet_contest.ui.components.text.IconWithText
import com.github.tim06.wallet_contest.ui.components.topBar.TonTopAppBar

@Composable
fun InfoScreen(
    modifier: Modifier = Modifier,
    buttonsHorizontalPadding: Dp = 80.dp,
    @RawRes icon: Int,
    title: String,
    description: String? = null,
    firstButtonText: String,
    firstButtonClick: () -> Unit,
    secondButtonText: String? = null,
    secondButtonClick: (() -> Unit)? = null,
    backClickListener: (() -> Unit)? = null,
    additionalContent: @Composable (ColumnScope.() -> Unit)? = null
) {
    InfoScreen(
        modifier = modifier
            .background(color = MaterialTheme.colors.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        top = {
            if (backClickListener != null) {
                TonTopAppBar(title = "", backClick = backClickListener)
            }
        },
        center = {
            IconWithText(
                modifier = Modifier.padding(horizontal = 40.dp),
                icon = icon,
                title = title,
                description = description
            )
        },
        bottom = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = buttonsHorizontalPadding)
                    .padding(bottom = 100.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                additionalContent?.invoke(this)
                if (secondButtonText != null && secondButtonClick != null) {
                    DoubleVerticalButtons(
                        firstButtonText = firstButtonText,
                        secondButtonText = secondButtonText,
                        fistButtonClick = firstButtonClick,
                        secondButtonClick = secondButtonClick
                    )
                } else {
                    TonButton(
                        modifier = Modifier.widthIn(min = 200.dp),
                        text = firstButtonText,
                        click = firstButtonClick
                    )
                }
            }
        }
    )
}

@Composable
fun InfoScreen(
    modifier: Modifier = Modifier,
    top: @Composable (BoxScope.() -> Unit)? = null,
    center: @Composable (BoxScope.() -> Unit)? = null,
    bottom: @Composable (BoxScope.() -> Unit)? = null
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        top?.let {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter), content = top
            )
        }
        center?.let {
            Box(
                modifier = Modifier
                    .align(Alignment.Center), content = center
            )
        }
        bottom?.let {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter), content = bottom
            )
        }
    }
}
