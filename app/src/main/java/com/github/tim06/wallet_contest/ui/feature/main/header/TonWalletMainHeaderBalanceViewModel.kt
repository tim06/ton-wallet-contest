package com.github.tim06.wallet_contest.ui.feature.main.header

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class TonWalletMainHeaderBalanceViewModel : ViewModel() {

    var isFirstReveal = true

    fun onRevealed() {
        isFirstReveal = false
    }
}

class TonWalletMainHeaderBalanceViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TonWalletMainHeaderBalanceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TonWalletMainHeaderBalanceViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}