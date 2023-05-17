package com.github.tim06.wallet_contest.ui.components.text

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.tim06.wallet_contest.ui.theme.PrimaryTextColor
import com.github.tim06.wallet_contest.ui.theme.TitleTextColor

@Composable
fun TitleWithDescription(
    modifier: Modifier = Modifier,
    title: String? = null,
    titleColor: Color = TitleTextColor,
    description: String? = null,
    descriptionColor: Color = PrimaryTextColor
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        title?.let {
            Text(
                text = title,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.body1.copy(
                    color = titleColor
                )
            )
        }
        description?.let {
            Text(
                text = description,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.body2.copy(
                    color = descriptionColor
                )
            )
        }
    }
}