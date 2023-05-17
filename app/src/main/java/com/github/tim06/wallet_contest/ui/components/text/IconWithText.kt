package com.github.tim06.wallet_contest.ui.components.text

import androidx.annotation.RawRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.github.tim06.wallet_contest.R
import com.github.tim06.wallet_contest.ui.components.rlottie.LottieIcon
import com.github.tim06.wallet_contest.ui.theme.PrimaryTextColor

@Composable
fun IconWithText(
    modifier: Modifier = Modifier,
    @RawRes icon: Int,
    title: String? = null,
    description: String? = null,
    descriptionColor: Color = PrimaryTextColor
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LottieIcon(modifier = Modifier.size(100.dp), icon = icon, iconSize = DpSize(100.dp, 100.dp))
        TitleWithDescription(title = title, description = description, descriptionColor = descriptionColor)
    }
}

@Preview(showSystemUi = true)
@Composable
private fun IconWithTextPreview() {
    IconWithText(
        icon = R.raw.congratulations,
        title = "Title",
        description = "Description"
    )
}