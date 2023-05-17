package com.github.tim06.wallet_contest.ui.feature.tonConnect.model

@kotlinx.serialization.Serializable
data class Feature(
    val name: String,
    val maxMessages: Int? = null
)
