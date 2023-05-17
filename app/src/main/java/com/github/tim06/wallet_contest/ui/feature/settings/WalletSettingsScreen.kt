package com.github.tim06.wallet_contest.ui.feature.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.tim06.wallet_contest.R
import com.github.tim06.wallet_contest.storage.PrimaryCurrency
import com.github.tim06.wallet_contest.storage.WalletVersion
import com.github.tim06.wallet_contest.ton.TonWalletClient
import com.github.tim06.wallet_contest.ui.components.topBar.TonTopAppBar
import com.github.tim06.wallet_contest.ui.theme.DividerColor1
import com.github.tim06.wallet_contest.ui.theme.ErrorColor
import com.github.tim06.wallet_contest.util.SystemBarIconsDark
import com.github.tim06.wallet_contest.util.transformAddress
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun WalletSettingsScreen(
    walletClient: TonWalletClient,
    viewModel: WalletSettingsViewModel = viewModel(
        factory = WalletSettingsViewModelFactory(walletClient)
    ),
    onBackClick: () -> Unit,
    onListOfTokensClick: () -> Unit,
    ondAppsClick: () -> Unit,
    onShowRecoveryPhraseClick: () -> Unit,
    onChangePasscodeClick: () -> Unit
) {
    SystemBarIconsDark(isDark = false)
    val coroutineScope = rememberCoroutineScope()
    val notificationsEnabled by viewModel.isNotificationsEnabled.collectAsState(initial = true)
    val activeAddress by viewModel.currentWalletVersion.collectAsState(initial = WalletVersion.V3R2)
    val primaryCurrency by viewModel.currentPrimaryCurrency.collectAsState(initial = PrimaryCurrency.USD)
    val biometricEnabled by viewModel.isBiometricAuthEnabled.collectAsState(initial = true)
    val walletSettingsModelFlow by viewModel.walletSettingsModelFlow.collectAsState(initial = emptyList())

    var activeAddressRowYOffset by remember { mutableStateOf(Float.NaN) }
    val isActiveAddressVersionModalActive by viewModel.isActiveAddressVersionSheetActive.collectAsState()

    val bottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )

    BackHandler {
        if (isActiveAddressVersionModalActive) {
            viewModel.onActiveAddressSheetDismissed()
        } else {
            onBackClick.invoke()
        }
    }

    ModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetShape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp),
        sheetBackgroundColor = MaterialTheme.colors.background,
        sheetContent = {
            PrimaryCurrencyBottomSheetContent(
                selectedCurrency = primaryCurrency,
                onCurrencyClick = {
                    coroutineScope.launch {
                        bottomSheetState.hide()
                    }
                    viewModel.onPrimaryCurrencySelected(it)
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.Black)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            TonTopAppBar(
                title = stringResource(id = R.string.settings_title),
                titleColor = Color.White,
                backIconColor = Color.White,
                backgroundColor = Color.Black,
                backClick = onBackClick
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colors.background,
                        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                    )
            ) {
                WalletSettingsItemTitle(text = stringResource(id = R.string.settings_general_title))
                WalletSettingsItemSwitch(
                    text = stringResource(id = R.string.settings_general_notifications),
                    checked = notificationsEnabled,
                    onCheckedChange = { viewModel.onNotificationsClick() }
                )
                WalletSettingsItemText(
                    modifier = Modifier
                        .onGloballyPositioned { layoutCoordinates ->
                            val height = layoutCoordinates.size.height / 2
                            activeAddressRowYOffset = layoutCoordinates.positionInRoot().y + height
                        }
                        .clickable(role = Role.DropdownList) {
                            viewModel.onActiveAddressClick()
                        },
                    text = stringResource(id = R.string.settings_general_active_address),
                    rightText = activeAddress.toString()
                )
                WalletSettingsItemText(
                    modifier = Modifier
                        .clickable(role = Role.DropdownList) {
                            coroutineScope.launch {
                                bottomSheetState.show()
                            }
                        },
                    text = stringResource(id = R.string.settings_general_primary_currency),
                    rightText = primaryCurrency.toString()
                )
                WalletSettingsItem(
                    modifier = Modifier
                        .clickable(role = Role.DropdownList, onClick = onListOfTokensClick),
                    text = stringResource(id = R.string.settings_general_list_of_tokens),
                    showDivider = true
                )
                WalletSettingsItem(
                    modifier = Modifier
                        .clickable(role = Role.DropdownList, onClick = ondAppsClick),
                    text = stringResource(id = R.string.settings_general_dapps),
                    showDivider = false
                )
                WalletSettingsItemTitle(text = stringResource(id = R.string.settings_security_title))
                WalletSettingsItem(
                    modifier = Modifier
                        .clickable(role = Role.DropdownList, onClick = onShowRecoveryPhraseClick),
                    text = stringResource(id = R.string.settings_security_show_recovery_phrase)
                )
                WalletSettingsItem(
                    modifier = Modifier
                        .clickable(role = Role.DropdownList, onClick = onChangePasscodeClick),
                    text = stringResource(id = R.string.settings_security_change_passcode)
                )
                if (walletClient.isDeviceBiometricAllowed) {
                    WalletSettingsItemSwitch(
                        text = stringResource(id = R.string.settings_security_biometric_auth),
                        checked = biometricEnabled,
                        onCheckedChange = { viewModel.onBiometricAuthClick() }
                    )
                }
                WalletSettingsItem(
                    text = stringResource(id = R.string.settings_security_delete_wallet),
                    textColor = ErrorColor,
                    showDivider = false
                )
            }
        }

        if (isActiveAddressVersionModalActive) {
            Popup(
                properties = PopupProperties(),
                alignment = Alignment.TopEnd,
                offset = IntOffset(x = 0, y = activeAddressRowYOffset.roundToInt()),
                onDismissRequest = viewModel::onActiveAddressSheetDismissed
            ) {
                Card(
                    modifier = Modifier.padding(end = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = MaterialTheme.colors.background,
                    elevation = 3.dp
                ) {
                    Column(modifier = Modifier.width(IntrinsicSize.Max)) {
                        walletSettingsModelFlow.forEachIndexed { index, model ->
                            ActiveAddressMenuRow(
                                modifier = Modifier
                                    .clickable(role = Role.Button) {
                                        viewModel.onActiveAddressSelected(model.version)
                                    },
                                version = model.version,
                                address = model.address.transformAddress(),
                                selected = model.current,
                                last = index == walletSettingsModelFlow.lastIndex
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveAddressMenuRow(
    modifier: Modifier = Modifier,
    version: WalletVersion,
    address: String,
    selected: Boolean,
    last: Boolean
) {
    Box(modifier = modifier.width(IntrinsicSize.Max)) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = version.toString(),
                style = MaterialTheme.typography.body2
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = address,
                style = MaterialTheme.typography.body2
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                modifier = Modifier.alpha(if (selected) 1f else 0f),
                imageVector = Icons.Default.Check,
                contentDescription = "selected",
                tint = MaterialTheme.colors.primary
            )
        }
        if (last.not()) {
            Divider(
                modifier = Modifier.align(Alignment.BottomStart),
                color = DividerColor1,
                thickness = 0.5.dp
            )
        }
    }
}

@Composable
private fun PrimaryCurrencyBottomSheetContent(
    currencies: Array<PrimaryCurrency> = PrimaryCurrency.values(),
    selectedCurrency: PrimaryCurrency,
    onCurrencyClick: (PrimaryCurrency) -> Unit
) {
    Column(
        modifier = Modifier.navigationBarsPadding()
    ) {
        currencies.forEachIndexed { index, currency ->
            PrimaryCurrencyBottomSheetRowItem(
                currency = currency,
                selected = currency == selectedCurrency,
                last = index == currencies.lastIndex,
                onCurrencyClick = onCurrencyClick
            )
        }
    }
}

@Composable
private fun PrimaryCurrencyBottomSheetRowItem(
    currency: PrimaryCurrency,
    selected: Boolean,
    last: Boolean,
    onCurrencyClick: (PrimaryCurrency) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.DropdownList) {
                onCurrencyClick.invoke(currency)
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.padding(vertical = 8.dp),
                text = currency.name,
                style = MaterialTheme.typography.body1
            )
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "selected",
                    tint = MaterialTheme.colors.primary
                )
            }
        }
        if (last.not()) {
            Divider(
                modifier = Modifier.align(Alignment.BottomStart),
                color = DividerColor1,
                thickness = 0.5.dp
            )
        }
    }
}