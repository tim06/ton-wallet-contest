package com.github.tim06.wallet_contest.ui.feature.tonConnect.model

@kotlinx.serialization.Serializable
data class DeviceInfo(
    val platform: String = "android",
    val appName: String = "Ton-wallet-contest",
    val appVersion: String = "0.0.1",
    val maxProtocolVersion: String = "2",
    // TODO broken json, check sources of tonconnect
    val features: List<String> = listOf(
        "SendTransaction"
    )
)
