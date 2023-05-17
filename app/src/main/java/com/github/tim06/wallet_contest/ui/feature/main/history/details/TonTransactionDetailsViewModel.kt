package com.github.tim06.wallet_contest.ui.feature.main.history.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.tim06.wallet_contest.storage.transaction.RawTransaction
import com.github.tim06.wallet_contest.ton.TonWalletClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TonTransactionDetailsViewModel(
    private val tonWalletClient: TonWalletClient
) : ViewModel() {

    private val _transaction = MutableStateFlow<RawTransaction?>(null)
    val transaction = _transaction.asStateFlow()

    fun loadTransaction(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _transaction.value = tonWalletClient.getTransactionWithId(id)
        }
    }
}

class TonTransactionDetailsViewModelFactory(
    private val tonWalletClient: TonWalletClient
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TonTransactionDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TonTransactionDetailsViewModel(
                tonWalletClient
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}