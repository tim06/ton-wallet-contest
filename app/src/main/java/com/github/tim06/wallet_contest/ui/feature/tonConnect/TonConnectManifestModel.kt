package com.github.tim06.wallet_contest.ui.feature.tonConnect

@kotlinx.serialization.Serializable
data class TonConnectManifestModel(
    val url: String,
    val name: String,
    val iconUrl: String,
    val termsOfUseUrl: String? = null,
    val privacyPolicyUrl: String? = null
)
