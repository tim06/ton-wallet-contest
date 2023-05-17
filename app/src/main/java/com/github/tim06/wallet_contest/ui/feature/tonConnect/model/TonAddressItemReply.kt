package com.github.tim06.wallet_contest.ui.feature.tonConnect.model

/**
 *
 * @param address TON address raw (`0:<hex>`)
 * @param network MAINNET = '-239', TESTNET = '-3'
 * @param publicKey HEX string without 0x
 * @param walletStateInit Base64 (not url safe) encoded stateinit cell for the wallet contract
 */
@kotlinx.serialization.Serializable
data class TonAddressItemReply(
    override val name: String = "ton_addr",
    val address: String,
    val network: String = "-239",
    val publicKey: String,
    val walletStateInit: String
) : ConnectItemReply
