package com.github.tim06.wallet_contest.ui.feature.create.perfrect

import androidx.compose.foundation.layout.*
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.tim06.wallet_contest.R
import com.github.tim06.wallet_contest.storage.InputKeyRegular
import com.github.tim06.wallet_contest.storage.toTonApiKeyRegular
import com.github.tim06.wallet_contest.ton.TonWalletClient
import com.github.tim06.wallet_contest.ui.components.InfoScreen
import com.github.tim06.wallet_contest.ui.theme.DividerColor1
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Composable
fun CreatePerfectScreen(
    tonWalletClient: TonWalletClient,
    inputKey: String? = null,
    onSetPasscodeClick: () -> Unit,
    onBackClickListener: () -> Unit
) {
    var checked by remember { mutableStateOf(true) }
    InfoScreen(
        icon = R.raw.success,
        title = stringResource(id = R.string.create_perfect_title),
        description = stringResource(id = R.string.create_perfect_description),
        firstButtonText = stringResource(id = R.string.create_perfect_button),
        firstButtonClick = {
            inputKey?.let { key ->
                val regularKey = Json.decodeFromString<InputKeyRegular>(key)
                tonWalletClient.finishCreateImportWallet(regularKey.toTonApiKeyRegular()/*, checked*/)
            }
            onSetPasscodeClick.invoke()
        },
        backClickListener = onBackClickListener,
        additionalContent = {
            Column {
                if (tonWalletClient.isDeviceBiometricAllowed) {
                    BiometricCheckbox(modifier = Modifier.offset(x = (-6).dp), checked = checked) {
                        checked = checked.not()
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    )
}

@Composable
private fun BiometricCheckbox(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            colors = CheckboxDefaults.colors(
                checkmarkColor = Color.White,
                checkedColor = MaterialTheme.colors.primary,
                uncheckedColor = DividerColor1
            ),
            onCheckedChange = onCheckedChange
        )
        Text(
            modifier = Modifier.offset(x = (-4).dp),
            text = stringResource(id = R.string.create_perfect_option),
            style = MaterialTheme.typography.body2.copy(
                fontSize = 14.sp,
                lineHeight = 18.sp
            )
        )
    }
}