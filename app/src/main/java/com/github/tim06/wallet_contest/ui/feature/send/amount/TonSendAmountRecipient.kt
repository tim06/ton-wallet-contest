package com.github.tim06.wallet_contest.ui.feature.send.amount

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.github.tim06.wallet_contest.R
import com.github.tim06.wallet_contest.ui.theme.InterRegular
import com.github.tim06.wallet_contest.ui.theme.SecondaryTextColor
import com.github.tim06.wallet_contest.util.calculateTextWidth

@Composable
fun TonSendAmountRecipient(
    modifier: Modifier = Modifier,
    data: TonSendAmountRecipientData? = null,
    onEditClick: () -> Unit
) {
    val spaceWidth = calculateTextWidth(
        style = MaterialTheme.typography.body2.copy(
            fontFamily = InterRegular
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(spaceWidth)
            ) {
                Text(
                    text = stringResource(id = R.string.send_send_to),
                    style = MaterialTheme.typography.body2.copy(
                        fontFamily = InterRegular,
                        color = SecondaryTextColor
                    )
                )
                Text(
                    text = data?.address.orEmpty(),
                    style = MaterialTheme.typography.body2.copy(
                        fontFamily = InterRegular,
                        color = Color.Black
                    )
                )
                data?.nickname?.let {
                    Text(
                        text = data.nickname,
                        style = MaterialTheme.typography.body2.copy(
                            fontFamily = InterRegular,
                            color = SecondaryTextColor
                        )
                    )
                }
            }
            Text(
                modifier = Modifier.clickable(role = Role.Button, onClick = onEditClick),
                text = stringResource(id = R.string.send_edit),
                style = MaterialTheme.typography.body2.copy(
                    color = MaterialTheme.colors.primary
                )
            )
        }
    }
}

data class TonSendAmountRecipientData(
    val address: String,
    val nickname: String? = null
)