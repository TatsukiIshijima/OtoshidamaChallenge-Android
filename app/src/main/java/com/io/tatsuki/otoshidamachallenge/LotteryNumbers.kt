package com.io.tatsuki.otoshidamachallenge

import com.squareup.moshi.Moshi

data class LotteryNumbers(
    val specialPrimaryForward: String = "",
    val specialPrimaryBackward: String = "",
    val specialSecondaryForward: String = "",
    val specialSecondaryBackward: String = "",
    val specialTertiaryForward: String = "",
    val specialTertiaryBackward: String = "",
    val specialQuaternaryForward: String = "",
    val specialQuaternaryBackward: String = "",
    val specialQuinaryForward: String = "",
    val specialQuinaryBackward: String = "",
    val firstClass: String = "",
    val secondClass: String = "",
    val thirdClass: String = ""
)

fun LotteryNumbers.toJson(): String {
    val moshi = Moshi.Builder().build()
    val adapter = moshi.adapter(LotteryNumbers::class.java)
    return adapter.toJson(this)
}

fun String.toLotteryNumbers(): LotteryNumbers {
    val moshi = Moshi.Builder().build()
    val adapter = moshi.adapter(LotteryNumbers::class.java)
    return adapter.fromJson(this) ?: LotteryNumbers()
}