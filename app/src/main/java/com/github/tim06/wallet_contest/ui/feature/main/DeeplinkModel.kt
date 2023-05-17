package com.github.tim06.wallet_contest.ui.feature.main

data class DeeplinkModel(
    val walletAddress: String,
    val amount: String? = null,
    val message: String? = null
)
