package com.github.tim06.wallet_contest.ui.feature.main.history.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.tim06.wallet_contest.R
import com.github.tim06.wallet_contest.storage.transaction.RawTransaction
import com.github.tim06.wallet_contest.ui.components.rlottie.LottieIcon
import com.github.tim06.wallet_contest.ui.theme.BalanceBigTextStyle
import com.github.tim06.wallet_contest.ui.theme.ErrorColor
import com.github.tim06.wallet_contest.ui.theme.SansMedium
import com.github.tim06.wallet_contest.ui.theme.SuccessColor
import com.github.tim06.wallet_contest.util.formatCurrency

@Composable
fun TonTransactionDetailsContentCount(
    modifier: Modifier = Modifier,
    transaction: RawTransaction
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        LottieIcon(
            modifier = Modifier.size(44.dp, 56.dp),
            iconSize = DpSize(44.dp, 56.dp),
            icon = R.raw.main
        )
        Text(
            text = transaction.getAmount().formatCurrency().toAnnotated(transaction.isIncome()),
            style = BalanceBigTextStyle.copy(
                color = if (transaction.isIncome()) SuccessColor else ErrorColor
            )
        )
    }
}

private fun CharSequence.toAnnotated(isIncome: Boolean): AnnotatedString {
    val splited = split(".")
    val left = splited.first()
    val right = splited.last()
    return buildAnnotatedString {
        withStyle(
            style = SpanStyle(
                fontFamily = SansMedium,
                fontWeight = FontWeight.Medium,
                fontSize = 44.sp,
                color = if (isIncome) SuccessColor else ErrorColor
            )
        ) {
            append(left)
        }
        withStyle(
            style = SpanStyle(
                fontFamily = SansMedium,
                fontWeight = FontWeight.Medium,
                fontSize = 32.sp,
                color = if (isIncome) SuccessColor else ErrorColor
            )
        ) {
            append(".$right")
        }
    }
}