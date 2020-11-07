package com.chat.android.im.utils

import java.text.ParseException
import kotlin.jvm.Throws

interface ISO8601Converter {
    fun fromTimestamp(timestamp: Long): String

    @Throws(ParseException::class)
    fun toTimestamp(date: String): Long
}