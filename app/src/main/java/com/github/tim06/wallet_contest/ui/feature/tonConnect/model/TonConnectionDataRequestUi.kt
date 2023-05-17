package com.github.tim06.wallet_contest.ui.feature.tonConnect.model

import com.github.tim06.wallet_contest.storage.WalletVersion

data class TonConnectionDataRequestUi(
    val data: TonConnectConnectionData,
    val address: String,
    val walletVersion: WalletVersion
)