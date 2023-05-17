package com.github.tim06.wallet_contest.util

fun String.transformAddress(count: Int = 4): String {
    return take(count) + "…" + takeLast(count)
}

fun String.splitAddressToTwoLines(): String {
    return take(count() / 2) + "\n" + takeLast(count() / 2)
}
