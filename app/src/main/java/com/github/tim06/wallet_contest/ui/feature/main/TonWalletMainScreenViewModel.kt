package com.github.tim06.wallet_contest.ui.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TonWalletMainScreenViewModel : ViewModel() {

    private val _bottomSheetExpanded = MutableStateFlow(false)
    val bottomSheetExpanded = _bottomSheetExpanded.asStateFlow()

    fun onBottomSheetStateChange(expand: Boolean) {
        _bottomSheetExpanded.value = expand
    }
}

class TonWalletMainScreenViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TonWalletMainScreenViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TonWalletMainScreenViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}