package com.github.tim06.wallet_contest.ui.feature.tonConnect.model.payload

import com.github.tim06.wallet_contest.ui.feature.tonConnect.model.ConnectItemReply
import com.github.tim06.wallet_contest.ui.feature.tonConnect.model.DeviceInfo

@kotlinx.serialization.Serializable
data class TonConnectConnectionPayload(
    val items: List<ConnectItemReply>,
    val device: DeviceInfo
) : TonConnectPayload