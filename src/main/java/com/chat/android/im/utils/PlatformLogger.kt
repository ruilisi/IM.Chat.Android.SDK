package com.chat.android.im.utils

interface PlatformLogger {
    fun debug(s: String)
    fun info(s: String)
    fun warn(s: String)

    class NoOpLogger : PlatformLogger {
        override fun debug(s: String) {
        }

        override fun info(s: String) {
        }

        override fun warn(s: String) {
        }
    }
}