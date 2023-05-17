package com.github.tim06.wallet_contest.storage

import drinkless.org.ton.TonApi

@kotlinx.serialization.Serializable
data class AccountAddress(
    val accountAddress: String
)

fun TonApi.AccountAddress.toStorage(): AccountAddress = AccountAddress(accountAddress)
fun AccountAddress.toApi(): TonApi.AccountAddress = TonApi.AccountAddress(accountAddress)