package com.github.tim06.wallet_contest.ui.feature.receive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.tim06.wallet_contest.ton.TonWalletClient
import kotlinx.coroutines.flow.*

class TonReceiveViewModel(
    client: TonWalletClient
) : ViewModel() {

    val walletAddress = client.getCurrentWalletFlow().filterNotNull().map { walletData ->
        walletData.address
    }

    val qrData = client.getCurrentWalletFlow().filterNotNull().map { walletData ->
        "ton://transfer/${walletData.address}"
    }
}

class TonReceiveViewModelFactory(
    private val tonWalletClient: TonWalletClient
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TonReceiveViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TonReceiveViewModel(
                tonWalletClient
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}