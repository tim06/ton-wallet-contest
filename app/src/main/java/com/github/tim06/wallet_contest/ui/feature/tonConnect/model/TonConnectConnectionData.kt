package com.github.tim06.wallet_contest.ui.feature.tonConnect.model

import com.github.tim06.wallet_contest.ui.feature.tonConnect.TonConnectManifestModel

@kotlinx.serialization.Serializable
data class TonConnectConnectionData(
    val manifest: TonConnectManifestModel,
    val id: String,
    val key: TonConnectConnectionKeyData,
    val walletAddress: String,
    val lastEventId: Long = -1,
    val payload: String? = null
)