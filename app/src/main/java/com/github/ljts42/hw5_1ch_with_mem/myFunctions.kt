package com.github.ljts42.hw5_1ch_with_mem

import java.text.SimpleDateFormat
import java.util.*

fun String.asTime(): String {
    val time = Date(this.toLong())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    return timeFormat.format(time)
}
