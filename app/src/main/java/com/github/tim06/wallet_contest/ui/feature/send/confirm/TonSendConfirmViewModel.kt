package com.github.tim06.wallet_contest.ui.feature.send.confirm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.tim06.wallet_contest.ton.TonWalletClient
import com.github.tim06.wallet_contest.ui.feature.send.TonSendRecipientModel
import com.github.tim06.wallet_contest.util.formatCurrency
import com.github.tim06.wallet_contest.util.round
import com.github.tim06.wallet_contest.util.toTonLong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class TonSendConfirmViewModel(
    private val walletClient: TonWalletClient,
    address: String? = null,
    private val amount: Long? = null,
    private val commentExtra: String? = null
) : ViewModel() {

    private val _address = MutableStateFlow("")
    val addressFlow = _address.asStateFlow()

    private val _fee = MutableStateFlow("")
    val fee = _fee.asStateFlow()

    private val _error = MutableStateFlow(false)
    val error = _error.asStateFlow()

    private val _notEnoughFundsError = MutableStateFlow(false)
    val notEnoughFundsError = _notEnoughFundsError.asStateFlow()

    private val addr: TonSendRecipientModel? =
        address?.let { Json.decodeFromString<TonSendRecipientModel>(it) }

    init {
        addr?.let { model ->
            if (model.address.isNotEmpty()) {
                if (model.address.contains(":")) {
                    viewModelScope.launch(Dispatchers.IO) {
                        walletClient.getAddressFromRaw(model.address)?.let { addressResult ->
                            _address.value = addressResult
                            loadFee(addressResult)
                        }
                    }
                } else {
                    _address.value = model.address
                    loadFee(model.address)
                }
            }
        }
    }

    private fun loadFee(address: String) {
        if (amount != null) {
            viewModelScope.launch(Dispatchers.IO) {
                val result = walletClient.calculateFee(address, amount, commentExtra)
                when (result) {
                    is TonWalletClient.FeeResponse.Success -> {
                        _error.value = false
                        _notEnoughFundsError.value = false
                        try {
                            _fee.value =
                                "≈ ${result.fee.formatCurrency().toString().toDouble().round(3)}"
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    is TonWalletClient.FeeResponse.Error -> {
                        _error.value = true
                    }

                    is TonWalletClient.FeeResponse.NotEnoughFunds -> {
                        _notEnoughFundsError.value = true
                    }
                }
            }
        }
    }
}

class TonSendConfirmViewModelFactory(
    private val tonWalletClient: TonWalletClient,
    private val address: String? = null,
    private val amount: Long? = null,
    private val commentExtra: String? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TonSendConfirmViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TonSendConfirmViewModel(
                tonWalletClient,
                address,
                amount,
                commentExtra
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}