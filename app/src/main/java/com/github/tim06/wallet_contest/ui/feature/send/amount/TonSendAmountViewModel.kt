package com.github.tim06.wallet_contest.ui.feature.send.amount

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.tim06.wallet_contest.ton.TonWalletClient
import com.github.tim06.wallet_contest.util.formatCurrency
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class TonSendAmountViewModel(
    private val tonWalletClient: TonWalletClient,
    private val amount: Long? = null
) : ViewModel() {

    private val _walletBalance = MutableStateFlow<CharSequence>("")
    val walletBalance = _walletBalance.asStateFlow()

    private val _sendAllChecked = MutableStateFlow(false)
    val sendAllChecked = _sendAllChecked.asStateFlow()

    private val _enteredAmount = MutableStateFlow(amount?.formatCurrency()?.toString() ?: "")
    val enteredAmount = _enteredAmount.asStateFlow()

    val error = _enteredAmount.map {
        if (it.isNotEmpty() && _walletBalance.value.isNotEmpty()) {
            it.toDouble() > _walletBalance.value.toString().toDouble()
        } else {
            false
        }
    }

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    init {
        loadWalletBalance()
    }

    fun onNumberClick(number: String) {
        val resultNumber = if (number == ".") {
            if (_enteredAmount.value.contains(".")) {
                ""
            } else {
                if (_enteredAmount.value.isEmpty()) {
                    "0$number"
                } else {
                    number
                }
            }
        } else {
            if (_enteredAmount.value == "0") {
                onDeleteClick()
            }
            number
        }
        _enteredAmount.value += resultNumber
    }

    fun onDeleteClick() {
        _enteredAmount.value = _enteredAmount.value.dropLast(1)
    }

    fun onSendAllCheckChanged(checked: Boolean) {
        _sendAllChecked.value = _sendAllChecked.value.not()
        _enteredAmount.value = _walletBalance.value.toString()
    }

    private fun loadWalletBalance() {
        viewModelScope.launch(Dispatchers.IO) {
            tonWalletClient.getCurrentWallet()?.balance?.let { balance ->
                _walletBalance.value = balance.formatCurrency()
            }
        }
    }
}

class TonSendAmountViewModelFactory(
    private val tonWalletClient: TonWalletClient,
    private val amount: Long? = 0L
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TonSendAmountViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TonSendAmountViewModel(
                tonWalletClient,
                amount
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}