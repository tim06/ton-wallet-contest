package com.github.tim06.wallet_contest.ui.feature.main.content

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.tim06.wallet_contest.ton.TonWalletClient
import kotlinx.coroutines.flow.flatMapLatest

class TonWalletMainScreenContentViewModel(private val tonWalletClient: TonWalletClient) : ViewModel() {

    val walletData = tonWalletClient.getCurrentWalletFlow()
    val transactions = walletData.flatMapLatest { walletData ->
        tonWalletClient.getTransactions(walletData?.address.orEmpty())
    }

    init {
        updateAccountState()
    }

    fun updateAccountState() {
        tonWalletClient.updateCurrentAccountState()
    }

}

class TonWalletMainScreenContentViewModelFactory(private val tonWalletClient: TonWalletClient) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TonWalletMainScreenContentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TonWalletMainScreenContentViewModel(tonWalletClient) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}