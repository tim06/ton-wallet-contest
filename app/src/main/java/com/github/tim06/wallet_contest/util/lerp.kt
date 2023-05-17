package com.github.tim06.wallet_contest.util

fun lerp(start: Float, stop: Float, amount: Float): Float {
    return (1 - amount) * start + amount * stop
}