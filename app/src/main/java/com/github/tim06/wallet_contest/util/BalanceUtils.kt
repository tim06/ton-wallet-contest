package com.github.tim06.wallet_contest.util

import android.text.TextUtils
import org.telegram.messenger.Utilities
import java.util.*
import kotlin.math.round

fun Long.formatCurrency(withSign: Boolean = false): CharSequence {
    if (this == 0L) {
        return "0"
    }
    val sign = if (this < 0 && withSign) {
        "-"
    } else {
        ""
    }
    val builder = StringBuilder(
        String.format(
            Locale.US,
            "%s%d.%09d",
            sign,
            Math.abs(this / 1000000000L),
            Math.abs(this % 1000000000)
        )
    )
    while (builder.length > 1 && builder[builder.length - 1] == '0' && builder[builder.length - 2] != '.') {
        builder.deleteCharAt(builder.length - 1)
    }
    return builder
}

fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return round(this * multiplier) / multiplier
}

fun String.toTonLong(): Long {
    val stringBuilder = StringBuilder(this)
    var index = TextUtils.indexOf(stringBuilder, '.')
    return if (index >= 0) {
        if (stringBuilder.length - index > 10) {
            stringBuilder.delete(index + 10, stringBuilder.length)
        }
        if (index > 9) {
            val countToDelete = index - 9
            stringBuilder.delete(9, 9 + countToDelete)
            index -= countToDelete
        }
        val start: String = stringBuilder.subSequence(0, index).toString()
        val end: String = stringBuilder.subSequence(index + 1, stringBuilder.length).toString()
        Utilities.parseLong(start) * 1000000000L + (Utilities.parseLong(end) * Math.pow(
            10.0,
            (9 - end.length).toDouble()
        )).toInt()
    } else {
        if (stringBuilder.length > 9) {
            stringBuilder.delete(9, stringBuilder.length)
        }
        Utilities.parseLong(stringBuilder.toString()) * 1000000000L;
    }
}