package com.github.tim06.wallet_contest.ui.feature.send.recepient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.tim06.wallet_contest.ton.TonWalletClient
import com.github.tim06.wallet_contest.ui.feature.send.TonSendRecipientModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class TonSendRecipientViewModel(
    private val tonWalletClient: TonWalletClient,
    private val recipientAddress: String? = null
) : ViewModel() {

    private val _address = MutableStateFlow(recipientAddress.orEmpty())
    val address = _address.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _error = MutableStateFlow("")
    val error = _error.asStateFlow()

    private val _success = MutableStateFlow(false)
    val success = _success.asStateFlow()

    var recipientModel: TonSendRecipientModel? = null

    val recentSendTransactions = tonWalletClient.getRawTransactionsFlow(recipientAddress.orEmpty()).map { transactions ->
        transactions.filter { it.isIncome().not() }.distinctBy { it.getDestinationOrSourceAddress() }.take(2)
    }

    fun onAddressChanged(newAddress: String) {
        _address.value = newAddress
    }

    fun onButtonClick() {
        if (_address.value.isNotEmpty()) {
            _loading.value = true
            viewModelScope.launch(Dispatchers.IO) {
                val isSuccess: Boolean
                if (_address.value.endsWith(".ton")) {
                    val resolvedAddress = tonWalletClient.resolveTonDnsAddress(_address.value)
                    if (resolvedAddress != null) {
                        isSuccess = true
                        recipientModel = TonSendRecipientModel(resolvedAddress, _address.value)
                    } else {
                        isSuccess = false
                    }
                } else {
                    val walletExist = tonWalletClient.isWalletAddressExist(_address.value)
                    if (walletExist) {
                        isSuccess = true
                        recipientModel = TonSendRecipientModel(_address.value)
                    } else {
                        isSuccess = false
                    }
                }
                if (isSuccess) {
                    _success.value = true
                    delay(200)
                    _success.value = false
                } else {
                    _error.value = "Not found"
                    delay(1000)
                    _error.value = ""
                }
                _loading.value = false
            }
        } else {
            // TODO error
        }
    }
}

class TonSendRecipientViewModelFactory(
    private val tonWalletClient: TonWalletClient,
    private val recipientAddress: String? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TonSendRecipientViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TonSendRecipientViewModel(tonWalletClient, recipientAddress) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}