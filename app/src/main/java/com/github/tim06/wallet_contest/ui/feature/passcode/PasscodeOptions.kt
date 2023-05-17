package com.github.tim06.wallet_contest.ui.feature.passcode

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.tim06.wallet_contest.R
import com.github.tim06.wallet_contest.ui.components.button.TonTextButton

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PasscodeOptions(
    modifier: Modifier = Modifier,
    clickEnabled: Boolean = true,
    onDigitsCountChanged: ((Int) -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }
    AnimatedVisibility(
        visible = expanded,
        enter = scaleIn() + slideInVertically { it / 2 },
        exit = scaleOut() + slideOutVertically { it / 2 }
    ) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(6.dp),
            elevation = 1.dp,
            backgroundColor = MaterialTheme.colors.background
        ) {
            Column(modifier = Modifier.width(220.dp)) {
                DropdownMenuItem(
                    contentPadding = PaddingValues(start = 20.dp, top = 15.dp, bottom = 13.dp),
                    onClick = {
                        expanded = false
                        onDigitsCountChanged?.invoke(4)
                    }
                ) {
                    Text(
                        text = stringResource(id = R.string.create_passcode_select_option, 4),
                        style = MaterialTheme.typography.body2.copy(
                            fontSize = 16.sp
                        )
                    )
                }
                DropdownMenuItem(
                    contentPadding = PaddingValues(start = 20.dp, top = 15.dp, bottom = 13.dp),
                    onClick = {
                        expanded = false
                        onDigitsCountChanged?.invoke(6)
                    }
                ) {
                    Text(
                        text = stringResource(id = R.string.create_passcode_select_option, 6),
                        style = MaterialTheme.typography.body2.copy(
                            fontSize = 16.sp
                        )
                    )
                }
            }
        }
    }
    TonTextButton(
        text = stringResource(id = R.string.create_passcode_options),
        click = {
            if (clickEnabled) {
                expanded = expanded.not()
            }
        }
    )
}