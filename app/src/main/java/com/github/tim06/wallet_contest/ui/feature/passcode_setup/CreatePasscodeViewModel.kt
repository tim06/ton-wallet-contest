package com.github.tim06.wallet_contest.ui.feature.passcode_setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.tim06.wallet_contest.ton.TonWalletClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CreatePasscodeViewModel(
    private val tonWalletClient: TonWalletClient,
    private val cachedPasscode: String? = null
) : ViewModel() {

    private val _passcodeCount = MutableStateFlow(
        value = cachedPasscode?.count() ?: 4
    )
    val passcodeCount = _passcodeCount.asStateFlow()

    private val _passcode = MutableStateFlow("")
    val passcode = _passcode.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _successAnimation = MutableStateFlow(false)
    val successAnimation = _successAnimation.asStateFlow()

    private val _errorAnimation = MutableStateFlow(false)
    val errorAnimation = _errorAnimation.asStateFlow()

    val isConfirm = cachedPasscode?.isNotEmpty() ?: false

    fun onCountChanged(newPasscodeCount: Int) {
        _passcodeCount.value = newPasscodeCount
        _passcode.value = ""
    }

    fun onNewDigitInPasscode(digit: String) {
        if (_passcode.value.count() < _passcodeCount.value) {
            _passcode.value += digit
        }
        if (_passcode.value.count() == _passcodeCount.value) {
            if (cachedPasscode.isNullOrEmpty()) {
                preparePasscode()
            } else {
                if (_passcode.value == cachedPasscode) {
                    savePasscode()
                } else {
                    viewModelScope.launch {
                        _errorAnimation.value = true
                    }
                }
            }
        }
    }

    fun onDeleteDigit() {
        _passcode.value = _passcode.value.dropLast(1)
    }

    fun onErrorAnimationEnd() {
        _errorAnimation.value = false
    }

    private fun preparePasscode() {
        viewModelScope.launch(Dispatchers.IO) {
            _loading.value = true
            tonWalletClient.prepareForPasscodeChange(_passcode.value)
            _successAnimation.value = true
        }
    }

    private fun savePasscode() {
        viewModelScope.launch(Dispatchers.IO) {
            _loading.value = true
            tonWalletClient.setUserPasscode(
                _passcode.value,
                if (_passcodeCount.value == 4) DIGITS_4 else DIGITS_6
            )
            _successAnimation.value = true
        }
    }

    private companion object {
        private const val DIGITS_4 = 0
        private const val DIGITS_6 = 1
    }
}

class CreatePasscodeViewModelFactory(
    private val tonWalletClient: TonWalletClient,
    private val cachedPasscode: String? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreatePasscodeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreatePasscodeViewModel(tonWalletClient, cachedPasscode) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}