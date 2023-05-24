package com.github.tim06.wallet_contest.ui.feature.tonConnect.transfer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.tim06.wallet_contest.ton.TonWalletClient
import com.github.tim06.wallet_contest.util.formatCurrency
import com.github.tim06.wallet_contest.util.round
import com.github.tim06.wallet_contest.util.toTonLong
import com.github.tim06.wallet_contest.util.transformAddress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TonConnectBottomSheetTransferViewModel(
    private val tonWalletClient: TonWalletClient,
    private val from: String,
    private val amount: String
) : ViewModel() {

    private val _state = MutableStateFlow<TonConnectTransferState>(TonConnectTransferState.Loading)
    val state = _state.asStateFlow()

    private val _buttonState = MutableStateFlow<TonConnectTransferButtonState>(TonConnectTransferButtonState.Empty)
    val buttonState = _buttonState.asStateFlow()

    private val amountToSend = amount.toLong().formatCurrency()
    private var recipientAddress = ""
    var awaitPasscode = false

    fun setNewRecipient(newRecipient: String) {
        if (newRecipient != recipientAddress) {
            recipientAddress = newRecipient
            loadData(newRecipient)
        }
    }

    fun onConfirmClick() {
        _buttonState.value = TonConnectTransferButtonState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            val privateKey = tonWalletClient.getPrivateKeyTemp()
            val address = tonWalletClient.getAddressFromRaw(recipientAddress)
            val sendResult = if (privateKey != null) {
                 tonWalletClient.sendTon(
                    privateKey,
                    null,
                    address.orEmpty(),
                    amount.toLong()
                )
            } else {
                false
            }
            _buttonState.value = if (sendResult is TonWalletClient.SendTonResponse.Success) {
                TonConnectTransferButtonState.Success
            } else {
                TonConnectTransferButtonState.Error
            }
        }
    }

    fun needPasscode(): Boolean {
        return tonWalletClient.isPrivateKeyWait().not()
    }

    fun clearStates() {
        _buttonState.value = TonConnectTransferButtonState.Empty
    }

    private fun loadData(recipient: String) {
        _state.value = TonConnectTransferState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            val address = getAddressFromRaw(recipient)
            if (address.isNullOrEmpty()) {
                _state.value = TonConnectTransferState.LoadingError("Invalid address")
            } else {
                calculateFee(address)
            }
        }
    }

    private suspend fun getAddressFromRaw(recipient: String): String? {
        return tonWalletClient.getAddressFromRaw(recipient)
    }

    private suspend fun calculateFee(address: String) {
        val feeResponse = tonWalletClient.calculateFee(address, amountToSend.toString().toTonLong())
        when (feeResponse) {
            is TonWalletClient.FeeResponse.Success -> _state.value = TonConnectTransferState.InfoLoaded(
                amount.toLong().formatCurrency().toString(),
                address.transformAddress(),
                feeResponse.fee.formatCurrency().toString().toDouble().round(3)
            )
            is TonWalletClient.FeeResponse.NotEnoughFunds -> _state.value = TonConnectTransferState.NotEnoughFunds
            is TonWalletClient.FeeResponse.Error -> _state.value = TonConnectTransferState.LoadingError("Unknown error!")
        }
    }
}

class TonConnectBottomSheetTransferViewModelFactory(
    private val tonWalletClient: TonWalletClient,
    private val from: String,
    private val amount: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TonConnectBottomSheetTransferViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TonConnectBottomSheetTransferViewModel(
                tonWalletClient,
                from,
                amount
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

sealed interface TonConnectTransferState {
    object Loading: TonConnectTransferState
    object NotEnoughFunds: TonConnectTransferState
    data class InfoLoaded(val amount: String, val recipient: String, val fee: Double): TonConnectTransferState
    data class LoadingError(val error: String): TonConnectTransferState
}

sealed class TonConnectTransferButtonState {
    object Empty: TonConnectTransferButtonState()
    object Loading: TonConnectTransferButtonState()
    object Success: TonConnectTransferButtonState()
    object Error: TonConnectTransferButtonState()
}