package com.github.tim06.wallet_contest.ui.feature.send.pending

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.tim06.wallet_contest.ton.TonWalletClient
import com.github.tim06.wallet_contest.util.formatCurrency
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TonSendPendingViewModel(
    private val walletClient: TonWalletClient,
    private val destination: String? = null,
    private val amount: String? = null,
    private val comment: String? = null
) : ViewModel() {

    private val _isSendSuccess = MutableStateFlow(false)
    val isSendSuccess = _isSendSuccess.asStateFlow()

    private val _isSendError = MutableStateFlow(false)
    val isSendError = _isSendError.asStateFlow()

    init {
        sendTon()
    }

    private fun sendTon() {
        viewModelScope.launch(Dispatchers.IO) {
            walletClient.getPrivateKeyTemp()?.let { keyRegular ->
                val result = walletClient.sendTon(
                    keyRegular = keyRegular,
                    message = comment.orEmpty(),
                    destinationAddress = destination.orEmpty(),
                    amount = amount?.toLong() ?: 0L
                )
                if (result is TonWalletClient.SendTonResponse.Success) {
                    _isSendSuccess.value = true
                } else {
                    _isSendError.value = true
                }
            }
        }
    }
}

class TonSendPendingViewModelFactory(
    private val tonWalletClient: TonWalletClient,
    private val destination: String? = null,
    private val amount: String? = null,
    private val comment: String? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TonSendPendingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TonSendPendingViewModel(
                tonWalletClient,
                destination,
                amount,
                comment
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}