package com.github.tim06.wallet_contest.ui.feature.import_wallet

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.tim06.wallet_contest.storage.InputKeyRegular
import com.github.tim06.wallet_contest.ton.TonWalletClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch

class ImportViewModel(private val tonWalletClient: TonWalletClient) : ViewModel() {

    private val _errorDialog = MutableStateFlow(false)
    val errorDialog = _errorDialog.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess = _isSuccess.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _suggestions = MutableStateFlow("")
    val suggestions = _suggestions
        .debounce(300)
        .mapNotNull { prefix ->
            if (prefix.count() >= 3) {
                tonWalletClient.getWordsSuggestion(prefix)
            } else {
                emptyArray()
            }
        }.flowOn(Dispatchers.IO)

    val words = mutableStateMapOf<Int, String>().apply {
        for (i in 1..24) {
            put(i, "")
        }
    }

    lateinit var inputKey: InputKeyRegular

    fun onRowChanged(key: Int, newText: String) {
        words[key] = newText
        _suggestions.value = newText
    }

    fun importWallet() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.emit(true)
            val inputKeyRegular = tonWalletClient.importWallet(words.values.toTypedArray())
            if (inputKeyRegular != null) {
                inputKey = inputKeyRegular
                _isSuccess.emit(true)
            } else {
                _errorDialog.emit(true)
                _isLoading.emit(false)
            }
        }
    }

    fun dismissErrorDialog() {
        _errorDialog.tryEmit(false)
    }

    fun clearSuggestions() {
        _suggestions.value = ""
    }
}

class ImportViewModelFactory(private val tonWalletClient: TonWalletClient) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ImportViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ImportViewModel(tonWalletClient) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}