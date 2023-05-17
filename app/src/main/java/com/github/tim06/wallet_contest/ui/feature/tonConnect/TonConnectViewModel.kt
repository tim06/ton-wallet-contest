package com.github.tim06.wallet_contest.ui.feature.tonConnect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.tim06.wallet_contest.storage.WalletVersion
import com.github.tim06.wallet_contest.ton.TonWalletClient
import com.github.tim06.wallet_contest.ui.feature.tonConnect.model.TonConnectConnectionData
import com.github.tim06.wallet_contest.ui.feature.tonConnect.model.TonConnectionDataRequestUi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TonConnectViewModel(
    private val tonWalletClient: TonWalletClient,
    private val tonConnectManager: TonConnectManager
) : ViewModel() {

    private val _bottomSheetShow = MutableStateFlow<BottomSheetState>(BottomSheetState.Empty)
    val bottomSheetShow = _bottomSheetShow.asStateFlow()

    private val _connectionRequest = MutableStateFlow<TonConnectionDataRequestUi?>(null)
    val connectionRequest = _connectionRequest.asStateFlow().onEach {
        if (it != null) {
            _bottomSheetShow.value = BottomSheetState.ConnectionRequest
        }
    }

    private val _transferRequest = tonConnectManager.events.flatMapLatest { list ->
        list.merge()
    }.onEach {
        _bottomSheetShow.value = BottomSheetState.TransferRequest
    }.mutableStateIn(viewModelScope, null)
    val transferRequest = _transferRequest.asStateFlow()

    private val _buttonState = MutableStateFlow<ButtonState>(ButtonState.Empty)
    val buttonState = _buttonState.asStateFlow()

    fun processString(tonConnectData: String) {
        _buttonState.value = ButtonState.Empty
        viewModelScope.launch(Dispatchers.IO) {
            val currentWalletData = tonWalletClient.getCurrentWallet()
            val currentWalletAddress = currentWalletData?.address.orEmpty()
            val currentWalletVersion = currentWalletData?.walletVersion ?: WalletVersion.V3R2
            val data = tonConnectManager.processString(tonConnectData, currentWalletAddress)
            _connectionRequest.tryEmit(
                TonConnectionDataRequestUi(
                    data = data,
                    currentWalletAddress,
                    currentWalletVersion
                )
            )
        }
    }

    fun approveConnectionRequest(data: TonConnectConnectionData) {
        _buttonState.value = ButtonState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            val rawAddress = tonWalletClient.getCurrentRawAddress()
            val publicKey = tonWalletClient.getCurrentWallet()?.publicKey
            val walletStateInit = tonWalletClient.getCurrentWalletStateInit()
            val privateKey = tonWalletClient.getPrivateKeyTempData()

            if (rawAddress != null && publicKey != null && walletStateInit != null && privateKey != null) {
                val isSuccess = tonConnectManager.approveTonConnectionRequest(
                    data,
                    rawAddress,
                    publicKey,
                    walletStateInit,
                    privateKey
                )
                if (isSuccess) {
                    _buttonState.value = ButtonState.Success
                } else {
                    _buttonState.value = ButtonState.Error
                }
            }
        }
    }

    fun rejectConnectionRequest(data: TonConnectionDataRequestUi) {
        viewModelScope.launch(Dispatchers.IO) {
            tonConnectManager.rejectConnectionRequest(data.data)
        }
    }

    fun rejectTonConnectionEvent(data: TonConnectConnectionData) {
        viewModelScope.launch(Dispatchers.IO) {
            tonConnectManager.rejectRequest(data)
        }
    }

    fun onBottomSheetDismiss() {
        val currentTransferData = _transferRequest.value
        if (currentTransferData != null) {
            if (currentTransferData is TonConnectEvent.TonConnectTransactionRequest) {
                rejectTonConnectionEvent(currentTransferData.data)
            }
        }
        val currentConnectionData = _connectionRequest.value
        if (currentConnectionData != null && _buttonState.value != ButtonState.Success) {
            rejectConnectionRequest(currentConnectionData)
        }
        _transferRequest.tryEmit(null)
        _connectionRequest.tryEmit(null)
        _bottomSheetShow.value = BottomSheetState.Empty
    }
}

public fun <T> Flow<T>.mutableStateIn(
    scope: CoroutineScope,
    initialValue: T
): MutableStateFlow<T> {
    val flow = MutableStateFlow(initialValue)

    scope.launch {
        this@mutableStateIn.collect(flow)
    }

    return flow
}

class TonConnectViewModelViewModelFactory(
    private val tonWalletClient: TonWalletClient,
    private val tonConnectManager: TonConnectManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TonConnectViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TonConnectViewModel(tonWalletClient, tonConnectManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

sealed interface ButtonState {
    object Empty: ButtonState
    object Loading: ButtonState
    object Success: ButtonState
    object Error: ButtonState
}

sealed interface BottomSheetState {
    object Empty: BottomSheetState
    object ConnectionRequest: BottomSheetState
    object TransferRequest: BottomSheetState
}