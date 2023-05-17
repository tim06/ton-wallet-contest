package com.github.tim06.wallet_contest.ui.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.tim06.wallet_contest.storage.PrimaryCurrency
import com.github.tim06.wallet_contest.storage.WalletVersion
import com.github.tim06.wallet_contest.ton.TonWalletClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class WalletSettingsViewModel(
    private val walletClient: TonWalletClient
) : ViewModel() {

    val isNotificationsEnabled: Flow<Boolean> = walletClient.getNotificationsFlow()
    val currentWalletVersion = walletClient.getCurrentWalletAddressVersion()
    val currentPrimaryCurrency = walletClient.getPrimaryCurrencyFlow()
    val isBiometricAuthEnabled: Flow<Boolean> = walletClient.getBiometricAuthFlow()
    val walletSettingsModelFlow: Flow<List<WalletSettingsModel>> = walletClient.getWalletSettingsModelFlow()

    private val _isActiveAddressVersionSheetActive = MutableStateFlow(false)
    val isActiveAddressVersionSheetActive = _isActiveAddressVersionSheetActive.asStateFlow()

    private val _isPrimaryCurrencySheetActive = MutableStateFlow(false)
    val isPrimaryCurrencySheetActive = _isPrimaryCurrencySheetActive.asStateFlow()

    private val _isDeleteWalletConfirmationDialogActive = MutableStateFlow(false)
    val isDeleteWalletConfirmationDialogActive = _isDeleteWalletConfirmationDialogActive.asStateFlow()

    fun onNotificationsClick() {
        viewModelScope.launch(Dispatchers.Main) {
            val isEnabled = isNotificationsEnabled.firstOrNull() ?: true
            walletClient.saveNotificationsState(isEnabled.not())
        }
    }

    fun onActiveAddressClick() {
        _isActiveAddressVersionSheetActive.value = true
    }

    fun onActiveAddressSheetDismissed() {
        _isActiveAddressVersionSheetActive.value = false
    }

    fun onActiveAddressSelected(version: WalletVersion) {
        _isActiveAddressVersionSheetActive.value = false
        viewModelScope.launch(Dispatchers.IO) {
            val currentWalletVersion = currentWalletVersion.firstOrNull()
            if (version != currentWalletVersion) {
                walletClient.saveCurrentAddressVersion(version)
            }
        }
    }

    fun onPrimaryCurrencyClick() {
        _isPrimaryCurrencySheetActive.value = true
    }

    fun onPrimaryCurrencySheetDismissed() {
        _isPrimaryCurrencySheetActive.value = false
    }

    fun onPrimaryCurrencySelected(currency: PrimaryCurrency) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentCurrency = currentPrimaryCurrency.firstOrNull()
            if (currentCurrency != currency) {
                walletClient.savePrimaryCurrency(currency)
            }
        }
    }

    fun onBiometricAuthClick() {
        viewModelScope.launch(Dispatchers.IO) {
            val isEnabled = isBiometricAuthEnabled.firstOrNull() ?: true
            walletClient.saveBiometricAuthState(isEnabled.not())
        }
    }

    fun onDeleteWalletClick() {

    }
}

class WalletSettingsViewModelFactory(private val tonWalletClient: TonWalletClient) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WalletSettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WalletSettingsViewModel(tonWalletClient) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class WalletSettingsModel(
    val address: String,
    val version: WalletVersion,
    val current: Boolean
)