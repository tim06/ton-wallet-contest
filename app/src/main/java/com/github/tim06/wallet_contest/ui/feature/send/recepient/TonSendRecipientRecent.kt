package com.github.tim06.wallet_contest.ui.feature.send.recepient

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.tim06.wallet_contest.R
import com.github.tim06.wallet_contest.storage.transaction.RawTransaction
import com.github.tim06.wallet_contest.ui.theme.DividerColor1
import com.github.tim06.wallet_contest.ui.theme.InterRegular
import com.github.tim06.wallet_contest.ui.theme.SecondaryTextColor
import com.github.tim06.wallet_contest.util.transformAddress
import org.telegram.time.FastDateFormat
import java.util.*

@Composable
fun TonSendRecipientRecent(
    modifier: Modifier = Modifier,
    recentItems: List<RawTransaction>,
    onItemClick: (String) -> Unit
) {
    Column(modifier = modifier) {
        Text(
            modifier = Modifier.padding(horizontal = 20.dp),
            text = stringResource(id = R.string.send_recent),
            style = MaterialTheme.typography.body2.copy(
                fontFamily = InterRegular,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                lineHeight = 16.sp,
                color = MaterialTheme.colors.primary
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Column {
            recentItems.forEach { tonSendRecentData ->
                TonSendRecipientRecentItem(
                    modifier = Modifier.clickable(role = Role.Button) {
                        onItemClick.invoke(
                            tonSendRecentData.getDestinationOrSourceAddress()
                        )
                    },
                    transaction = tonSendRecentData
                )
            }
        }
    }
}

@Composable
private fun TonSendRecipientRecentItem(
    modifier: Modifier = Modifier,
    transaction: RawTransaction
) {
    val dateFormat = remember {
        FastDateFormat.getInstance("MMMM d", Locale.ENGLISH)
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = transaction.getDestinationOrSourceAddress().transformAddress(),
                style = MaterialTheme.typography.body2.copy(
                    fontSize = 16.sp,
                    color = Color.Black
                )
            )
            Text(
                text = dateFormat.format(transaction.utime * 1000),
                style = MaterialTheme.typography.body2.copy(
                    lineHeight = 18.sp,
                    color = SecondaryTextColor
                )
            )
        }
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart),
            color = DividerColor1,
            thickness = 0.5.dp
        )
    }
}

data class TonSendRecipientRecentData(
    val first: String,
    val second: String
)