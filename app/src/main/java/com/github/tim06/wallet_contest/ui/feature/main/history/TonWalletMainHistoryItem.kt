package com.github.tim06.wallet_contest.ui.feature.main.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.tim06.wallet_contest.R
import com.github.tim06.wallet_contest.storage.transaction.msg.TransactionMessageState
import com.github.tim06.wallet_contest.ui.components.rlottie.LottieIcon
import com.github.tim06.wallet_contest.ui.theme.ChipBackgroundColor
import com.github.tim06.wallet_contest.ui.theme.ErrorColor
import com.github.tim06.wallet_contest.ui.theme.HistoryItemDividerColor
import com.github.tim06.wallet_contest.ui.theme.InterRegular
import com.github.tim06.wallet_contest.ui.theme.InterSemibold
import com.github.tim06.wallet_contest.ui.theme.RobotoMonoRegular
import com.github.tim06.wallet_contest.ui.theme.SansMedium
import com.github.tim06.wallet_contest.ui.theme.SecondaryTextColor
import com.github.tim06.wallet_contest.ui.theme.SuccessColor
import com.github.tim06.wallet_contest.util.formatCurrency

@Composable
fun TonWalletMainHistoryItem(
    modifier: Modifier = Modifier,
    income: Boolean,
    time: String,
    count: Long,
    address: String,
    fee: CharSequence?,
    message: TransactionMessageState,
    isFirst: Boolean
) {
    Box(
        modifier = modifier.fillMaxWidth()
            .padding(top = if (isFirst) 0.dp else 14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LottieIcon(
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.CenterVertically),
                    iconSize = DpSize(18.dp, 18.dp),
                    icon = R.raw.main
                )
                Text(
                    text = count.formatCurrency().toAnnotatedBalance(),
                    style = TextStyle(
                        fontFamily = InterSemibold,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        lineHeight = 20.sp,
                        color = if (income) SuccessColor else ErrorColor
                    )
                )
                Text(
                    modifier = Modifier.align(Alignment.Bottom),
                    text = stringResource(id = if (income) R.string.wallet_history_from else R.string.wallet_history_to),
                    style = MaterialTheme.typography.body2.copy(
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = SecondaryTextColor
                    )
                )
            }
            Text(
                text = address.toAnnotatedAddress(),
                style = MaterialTheme.typography.body2.copy(
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    color = Color.Black
                ),
            )
            fee?.let {
                Text(
                    text = stringResource(id = R.string.wallet_history_fee, it.toString()),
                    style = MaterialTheme.typography.body2.copy(
                        fontFamily = InterRegular,
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        color = SecondaryTextColor
                    )
                )
            }
            when (message) {
                is TransactionMessageState.Decrypting -> {
                    TonWalletMainHistoryItemMessage(stringResource(id = R.string.transaction_encrypted_message))
                }
                is TransactionMessageState.Success -> {
                    TonWalletMainHistoryItemMessage(message.message)
                }
                else -> Unit
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        Text(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 3.dp, end = 16.dp),
            text = time,
            style = MaterialTheme.typography.body2.copy(
                fontSize = 14.sp,
                lineHeight = 18.sp,
                color = SecondaryTextColor
            )
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .height(1.dp)
                .background(color = HistoryItemDividerColor)
        )
    }
}

@Composable
private fun TonWalletMainHistoryItemMessage(message: String) {
    Box(
        modifier = Modifier.background(
            color = ChipBackgroundColor,
            shape = RoundedCornerShape(
                topStart = 4.dp,
                topEnd = 10.dp,
                bottomStart = 10.dp,
                bottomEnd = 10.dp
            )
        )
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            text = message,
            style = MessageTextStyle
        )
    }
}

private fun String.toAnnotatedAddress(): AnnotatedString {
    return buildAnnotatedString {
        withStyle(
            style = SpanStyle(
                fontFamily = RobotoMonoRegular,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            )
        ) {
            append(take(6))
        }

        withStyle(
            style = SpanStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            )
        ) {
            append("…")
        }

        withStyle(
            style = SpanStyle(
                fontFamily = RobotoMonoRegular,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            )
        ) {
            append(takeLast(7))
        }
    }
}

private fun CharSequence.toAnnotatedBalance(): AnnotatedString {
    return buildAnnotatedString {
        val split = this@toAnnotatedBalance.split(".")
        withStyle(
            style = SpanStyle(
                fontFamily = InterRegular,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )
        ) {
            append(split.first())
        }
        withStyle(
            style = SpanStyle(
                fontFamily = SansMedium,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        ) {
            append(".${split.last()}")
        }
    }
}

val MessageTextStyle = TextStyle(
    fontFamily = InterRegular,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 18.sp,
    color = Color.Black
)