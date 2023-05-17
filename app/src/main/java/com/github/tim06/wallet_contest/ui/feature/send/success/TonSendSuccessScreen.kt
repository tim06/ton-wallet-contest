package com.github.tim06.wallet_contest.ui.feature.send.success

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.github.tim06.wallet_contest.R
import com.github.tim06.wallet_contest.ui.components.InfoScreen
import com.github.tim06.wallet_contest.ui.components.button.TonButton
import com.github.tim06.wallet_contest.ui.components.text.IconWithText
import com.github.tim06.wallet_contest.ui.feature.send.TonSendContainer
import com.github.tim06.wallet_contest.ui.theme.RobotoMonoRegular
import com.github.tim06.wallet_contest.util.splitAddressToTwoLines

@Composable
fun TonSendSuccessScreen(
    onViewWalletClick: () -> Unit,
    onCloseClick: () -> Unit
) {
    val amount by remember { mutableStateOf(2.2) }
    val address by remember { mutableStateOf("UQBFz01R2CU7YA8pevUaNIYEzi1mRo4cX-r3W2Dwx-WEAoKP") }
    TonSendContainer {
        InfoScreen(
            top = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopStart) {
                    IconButton(
                        modifier = Modifier.padding(4.dp),
                        onClick = onCloseClick
                    ) {
                        Icon(painter = painterResource(id = R.drawable.ic_close), contentDescription = "Close")
                    }
                }
            },
            center = {
                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    IconWithText(
                        modifier = Modifier.padding(horizontal = 40.dp),
                        icon = R.raw.success,
                        title = stringResource(id = R.string.send_sending_done_title),
                        description = stringResource(id = R.string.send_sending_done_description, amount.toString())
                    )
                    Text(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        text = address.splitAddressToTwoLines(),
                        style = MaterialTheme.typography.body2.copy(
                            fontFamily = RobotoMonoRegular,
                            color = Color.Black
                        )
                    )
                }
            },
            bottom = {
                TonButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    click = onViewWalletClick,
                    text = stringResource(id = R.string.send_sending_done_button)
                )
            }
        )
    }
}