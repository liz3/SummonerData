package com.toxayah.summonerdata.utils

fun toRomNum(entry:Int): String {
    when (entry) {
        1 -> return "I"
        2 -> return "II"
        3 -> return "III"
        4 -> return "IV"
        5 -> return "V"
    }

    return ""
}