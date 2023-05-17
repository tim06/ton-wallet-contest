package com.github.tim06.wallet_contest.ui.components.row

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.github.tim06.wallet_contest.R
import com.github.tim06.wallet_contest.ui.components.rlottie.LottieIcon
import com.github.tim06.wallet_contest.ui.components.shimmer.ShimmerAnimation
import com.github.tim06.wallet_contest.ui.theme.DividerColor1
import com.github.tim06.wallet_contest.ui.theme.RobotoRegular

@Composable
fun TonRow(
    leftText: String,
    rightText: String,
    showIcon: Boolean = false,
    rightTextLoading: Boolean = false,
    leftTextFont: FontFamily = RobotoRegular,
    rightTextFont: FontFamily = RobotoRegular,
    showDivider: Boolean = true
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                modifier = Modifier.padding(start = 20.dp, top = 14.dp, bottom = 14.dp),
                text = leftText,
                style = MaterialTheme.typography.body2.copy(
                    fontFamily = leftTextFont
                )
            )
            Row(
                modifier = Modifier.padding(end = 20.dp, top = 14.dp, bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (showIcon) {
                    LottieIcon(
                        modifier = Modifier.size(18.dp),
                        iconSize = DpSize(18.dp, 18.dp),
                        icon = R.raw.main
                    )
                }
                if (rightTextLoading) {
                    ShimmerAnimation(modifier = Modifier.size(40.dp, 18.dp).clip(RoundedCornerShape(10.dp)))
                } else {
                    Text(
                        text = rightText,
                        style = MaterialTheme.typography.body2.copy(
                            fontFamily = rightTextFont
                        )
                    )
                }
            }
        }
        if (showDivider) {
            Divider(modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart), color = DividerColor1, thickness = 0.5.dp)
        }
    }
}