package com.github.tim06.wallet_contest.ui.feature.create.recovery_phrase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.tim06.wallet_contest.ton.TonWalletClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class RecoveryPhraseViewModel(
    private val tonWalletClient: TonWalletClient,
    private val inputKeyRegular: String? = null
) : ViewModel() {

    private val _secretWords = MutableStateFlow<Array<String>>(emptyArray())
    val secretWords = _secretWords.asStateFlow()

    val randomWords: String
        get() {
            val words = _secretWords.value.mapIndexed { index, item ->
                index + 1 to item
            }.shuffled().take(3).sortedBy { it.first }
            return Json.encodeToString(words)
        }

    init {
        if (inputKeyRegular != null) {
            loadWords()
        }
    }

    private fun loadWords() {
        viewModelScope.launch(Dispatchers.IO) {
            val words =
                tonWalletClient.getSecretWords(Json.decodeFromString(inputKeyRegular.orEmpty()))
            if (words.isEmpty().not()) {
                _secretWords.value = words
            }
        }
    }
}

class RecoveryPhraseViewModelFactory(
    private val tonWalletClient: TonWalletClient,
    private val inputKeyRegular: String? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecoveryPhraseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecoveryPhraseViewModel(tonWalletClient, inputKeyRegular) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}